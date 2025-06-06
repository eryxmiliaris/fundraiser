package com.vb.fundraiser.service;

import com.vb.fundraiser.client.CurrencyConversionClient;
import com.vb.fundraiser.exception.box.*;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.exception.currency.InvalidMoneyAmountException;
import com.vb.fundraiser.exception.event.FundraisingEventNotFoundException;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.model.entity.BoxCurrencyAmount;
import com.vb.fundraiser.model.entity.CollectionBox;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CollectionBoxRepository;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import com.vb.fundraiser.util.PaginationValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionBoxService {
    private final CollectionBoxRepository boxRepository;
    private final FundraisingEventRepository eventRepository;
    private final CurrencyRepository currencyRepository;

    private final CurrencyConversionClient conversionClient;

    public CollectionBoxDTO registerNewBox() {
        CollectionBox saved = boxRepository.save(CollectionBox.builder().event(null).build());

        log.info("Registered new collection box with ID {}", saved.getId());
        return new CollectionBoxDTO(saved.getId(), false, true);
    }

    public Page<CollectionBoxDTO> getAllBoxes(
            int pageNumber,
            int pageSize,
            String sortDirection
    ) {
        String sortBy = "id";
        PaginationValidator.validate(pageNumber, pageSize, sortDirection);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(PaginationValidator.parseDirection(sortDirection), sortBy));

        Page<CollectionBox> boxes = boxRepository.findByIsDeletedFalse(pageable);

        Page<CollectionBoxDTO> dtos = boxes
                .map(box -> new CollectionBoxDTO(
                        box.getId(),
                        box.getEvent() != null,
                        isBoxEmpty(box))
                );

        log.info("Boxes page retrieved: page {}, size {}, totalElements {}",
                dtos.getNumber(), dtos.getSize(), dtos.getTotalElements());
        return dtos;
    }

    public void unregisterBox(Long boxId) {
        CollectionBox box = boxRepository.findByIdAndIsDeletedFalse(boxId)
                .orElseThrow(() -> {
                    log.warn("Box with ID {} not found for unregistering", boxId);
                    return new BoxNotFoundException(boxId);
                });

        box.setDeleted(true);
        box.getAmounts().forEach(a -> a.setDeleted(true));
        boxRepository.save(box);

        log.info("Unregistered box with ID {} and unregistered its currency amounts", boxId);
    }

    public CollectionBoxDTO assignBoxToEvent(Long boxId, Long eventId) {
        CollectionBox box = boxRepository.findByIdAndIsDeletedFalse(boxId)
                .orElseThrow(() -> {
                    log.warn("Box with ID {} not found for assignment", boxId);
                    return new BoxNotFoundException(boxId);
                });

        if (box.getEvent() != null) {
            log.warn("Attempt to assign box {} that is already assigned to event {}", boxId, box.getEvent().getId());
            throw new BoxAlreadyAssignedException(boxId);
        }

        if (!isBoxEmpty(box)) {
            log.warn("Attempt to assign box {} which is not empty", boxId);
            throw new NotEmptyBoxAssignmentException(boxId);
        }

        FundraisingEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with ID {} not found for box assignment", eventId);
                    return new FundraisingEventNotFoundException(eventId);
                });

        box.setEvent(event);
        boxRepository.save(box);

        log.info("Assigned box {} to event '{}'", boxId, event.getName());
        return new CollectionBoxDTO(box.getId(), true, false);
    }

    @Transactional
    public void addMoney(Long boxId, String currencyCode, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid money amount {} for box {}", amount, boxId);
            throw new InvalidMoneyAmountException(amount);
        }

        CollectionBox box = boxRepository.findByIdAndIsDeletedFalse(boxId)
                .orElseThrow(() -> {
                    log.warn("Box with ID {} not found for adding money", boxId);
                    return new BoxNotFoundException(boxId);
                });

        if (box.getEvent() == null) {
            log.warn("Attempt to add money to box {} which is not assigned to an event", boxId);
            throw new BoxNotAssignedException(boxId);
        }

        Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> {
                    log.warn("Currency '{}' not found when adding money to box {}", currencyCode, boxId);
                    return new CurrencyNotFoundException(currencyCode);
                });

        Optional<BoxCurrencyAmount> existing = box.getAmounts().stream()
                .filter(e -> e.getCurrency().getCode().equals(currencyCode))
                .findFirst();

        if (existing.isPresent()) {
            BoxCurrencyAmount entry = existing.get();
            entry.setAmount(entry.getAmount().add(amount));
        } else {
            BoxCurrencyAmount newEntry = BoxCurrencyAmount.builder()
                    .box(box)
                    .currency(currency)
                    .amount(amount)
                    .build();
            box.getAmounts().add(newEntry);
        }

        boxRepository.save(box);
        log.info("Added {} {} to box {}", amount, currencyCode, boxId);
    }

    @Transactional
    public void emptyBox(Long boxId) {
        CollectionBox box = boxRepository.findByIdAndIsDeletedFalse(boxId)
                .orElseThrow(() -> {
                    log.warn("Box with ID {} not found for emptying", boxId);
                    return new BoxNotFoundException(boxId);
                });

        FundraisingEvent event = box.getEvent();
        if (event == null) {
            log.warn("Attempted to empty box {} not assigned to any event", boxId);
            throw new BoxNotAssignedException(boxId);
        }

        if (isBoxEmpty(box)) {
            log.warn("Attempt to transfer money from box {} which is empty", boxId);
            throw new EmptyBoxMoneyTransferException(boxId);
        }

        Currency targetCurrency = event.getCurrency();
        BigDecimal totalTransferred = BigDecimal.ZERO;

        for (BoxCurrencyAmount amount : box.getAmounts()) {
            if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal converted = amount.getCurrency().getCode().equals(targetCurrency.getCode())
                    ? amount.getAmount()
                    : conversionClient.convert(amount.getAmount(), amount.getCurrency().getCode(), targetCurrency.getCode());

            totalTransferred = totalTransferred.add(converted);
            amount.setAmount(BigDecimal.ZERO);
        }

        event.setAccountBalance(event.getAccountBalance().add(totalTransferred));
        boxRepository.save(box);
        eventRepository.save(event);

        log.info("Emptied box {}. Transferred total {} {} to event '{}'",
                boxId, totalTransferred, targetCurrency.getCode(), event.getName());
    }

    private boolean isBoxEmpty(CollectionBox box) {
        return box.getAmounts().stream()
                .map(BoxCurrencyAmount::getAmount)
                .allMatch(a -> a == null || a.compareTo(BigDecimal.ZERO) <= 0);
    }
}
