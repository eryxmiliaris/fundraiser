package com.vb.fundraiser.service;

import com.vb.fundraiser.exception.event.FundraisingEventAlreadyExistsException;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import com.vb.fundraiser.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundraisingEventService {
    private final FundraisingEventRepository eventRepository;
    private final CurrencyRepository currencyRepository;

    public FundraisingEventDTO createEvent(String name, String currencyCode) {
        if (eventRepository.existsByName(name)) {
            log.warn("Attempted to create duplicate event with name '{}'", name);
            throw new FundraisingEventAlreadyExistsException(name);
        }

        Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> {
                    log.warn("Currency '{}' not found when creating event '{}'", currencyCode, name);
                    return new CurrencyNotFoundException(currencyCode);
                });

        FundraisingEvent event = FundraisingEvent.builder()
                .name(name)
                .currency(currency)
                .accountBalance(BigDecimal.ZERO)
                .build();
        FundraisingEvent saved = eventRepository.save(event);

        log.info("Created fundraising event '{}' in {} with ID {}", name, currency.getCode(), saved.getId());
        return toDTO(saved);
    }

    public Page<FundraisingEventDTO> getFinancialReport(
            int pageNumber,
            int pageSize,
            String sortField,
            String sortDirection
    ) {
        PaginationValidator.validate(pageNumber, pageSize, sortDirection);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(PaginationValidator.parseDirection(sortDirection), sortField));

        Page<FundraisingEvent> events = eventRepository.findAll(pageable);

        Page<FundraisingEventDTO> report = events.map(this::toDTO);

        log.info("Financial report page retrieved: page {}, size {}, totalElements {}",
                report.getNumber(), report.getSize(), report.getTotalElements());
        return report;
    }

    public String generateHtmlReport(String sortField, String sortDirection) {
        Pageable pageable = Pageable.unpaged(Sort.by(PaginationValidator.parseDirection(sortDirection), sortField));
        Page<FundraisingEvent> events = eventRepository.findAll(pageable);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Fundraising Report</title>");
        html.append("""
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #333; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                    tr:nth-child(even) { background-color: #fafafa; }
                </style>
                </head><body>
                <h1>Fundraising Events Report</h1>
                <table>
                <tr>
                    <th>Fundraising event name</th>
                    <th>Amount</th>
                    <th>Currency</th>
                </tr>
        """);

        for (FundraisingEvent event : events) {
            html.append("<tr>")
                    .append("<td>").append(event.getName()).append("</td>")
                    .append("<td>").append(event.getAccountBalance().setScale(2, RoundingMode.HALF_UP)).append("</td>")
                    .append("<td>").append(event.getCurrency().getCode()).append("</td>")
                    .append("</tr>");
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    private FundraisingEventDTO toDTO(FundraisingEvent event) {
        return new FundraisingEventDTO(
                event.getId(),
                event.getName(),
                event.getCurrency().getCode(),
                event.getAccountBalance().setScale(2, RoundingMode.HALF_UP)
        );
    }
}