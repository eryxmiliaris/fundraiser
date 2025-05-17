package com.vb.fundraiser.service;

import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundraisingEventService {
    private final FundraisingEventRepository eventRepository;
    private final CurrencyRepository currencyRepository;

    public FundraisingEventDTO createEvent(CreateEventRequest request) {
        Currency currency = currencyRepository.findByCode(request.currencyCode())
                .orElseThrow(() -> {
                    log.warn("Currency '{}' not found when creating event '{}'", request.currencyCode(), request.name());
                    return new CurrencyNotFoundException(request.currencyCode());
                });

        FundraisingEvent event = FundraisingEvent.builder()
                .name(request.name())
                .currency(currency)
                .accountBalance(BigDecimal.ZERO)
                .build();
        FundraisingEvent saved = eventRepository.save(event);

        log.info("Created fundraising event with ID {}", saved.getId());

        return new FundraisingEventDTO(
                saved.getId(),
                saved.getName(),
                saved.getCurrency().getCode(),
                saved.getAccountBalance()
        );
    }

    public List<FundraisingEventDTO> getFinancialReport() {
        List<FundraisingEvent> events = eventRepository.findAll();

        List<FundraisingEventDTO> report = events.stream()
                .map(event -> new FundraisingEventDTO(
                        event.getId(),
                        event.getName(),
                        event.getCurrency().getCode(),
                        event.getAccountBalance().setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();

        log.info("Financial report generated with {} entries", report.size());
        return report;
    }
}