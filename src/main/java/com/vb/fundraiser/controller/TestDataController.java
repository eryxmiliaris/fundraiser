package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.entity.BoxCurrencyAmount;
import com.vb.fundraiser.model.entity.CollectionBox;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CollectionBoxRepository;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Profile("dev")
@Slf4j
@RestController
@RequestMapping("/api/v1/test-data")
@RequiredArgsConstructor
public class TestDataController {
    private final FundraisingEventRepository eventRepository;
    private final CollectionBoxRepository boxRepository;
    private final CurrencyRepository currencyRepository;

    private static int eventCounter = 0;

    @PostMapping
    public ResponseEntity<String> createTestData() {
        Random random = new Random(312);
        List<String> currencyCodes = List.of("USD", "EUR", "GBP", "PLN", "JPY");
        Map<String, Currency> currencies = currencyCodes.stream()
                .map(code -> currencyRepository.findByCode(code).orElseThrow())
                .collect(Collectors.toMap(Currency::getCode, c -> c));

        List<FundraisingEvent> events = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String code = currencyCodes.get(i % currencyCodes.size());
            FundraisingEvent event = FundraisingEvent.builder()
                    .name("Charity Event " + eventCounter++)
                    .currency(currencies.get(code))
                    .accountBalance(BigDecimal.valueOf(10 + random.nextInt(100) + random.nextDouble()))
                    .build();
            events.add(event);
        }
        eventRepository.saveAll(events);

        List<CollectionBox> boxes = new ArrayList<>();
        List<BoxCurrencyAmount> amounts = new ArrayList<>();

        long boxId = 1;
        for (FundraisingEvent event : events) {
            for (int i = 0; i < 2; i++) {
                CollectionBox box = CollectionBox.builder()
                        .event(event)
                        .build();
                List<BoxCurrencyAmount> boxAmounts = new ArrayList<>();

                int numAmounts = 1 + random.nextInt(3); // 1–3 entries per box
                Set<Currency> assignedCurrencies = new HashSet<>();
                for (int j = 0; j < numAmounts; j++) {
                    Optional<Currency> currency = currencyRepository.findByCode(currencyCodes.get(random.nextInt(currencyCodes.size())));
                    if (!assignedCurrencies.add(currency.get())) continue;

                    BigDecimal amount = BigDecimal.valueOf(10 + random.nextInt(100) + random.nextDouble());

                    BoxCurrencyAmount boxAmount = BoxCurrencyAmount.builder()
                            .currency(currency.get())
                            .amount(amount)
                            .box(box)
                            .build();
                    boxAmounts.add(boxAmount);
                }

                box.setAmounts(boxAmounts);
                boxes.add(box);
                amounts.addAll(boxAmounts);
                boxId++;
            }
        }

        boxRepository.saveAll(boxes);

        log.info("Inserted {} events, {} boxes, and {} amounts",
                events.size(), boxes.size(), amounts.size());
        return ResponseEntity.ok("Bulk test data created");
    }
}
