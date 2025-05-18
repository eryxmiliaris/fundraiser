package com.vb.fundraiser.init;

import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.repository.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyInitializer {
    private final CurrencyRepository currencyRepository;

    @PostConstruct
    public void initCurrencies() {
        Set<String> javaCurrencies = java.util.Currency.getAvailableCurrencies().stream()
                .map(java.util.Currency::getCurrencyCode)
                .collect(Collectors.toSet());

        Set<String> existingCodes = currencyRepository.findAll().stream()
                .map(Currency::getCode)
                .collect(Collectors.toSet());

        List<Currency> newCurrencies = javaCurrencies.stream()
                .filter(code -> !existingCodes.contains(code))
                .map(code -> Currency.builder().code(code).build())
                .toList();

        if (!newCurrencies.isEmpty()) {
            currencyRepository.saveAll(newCurrencies);
            log.info("Initialized {} ISO fiat currencies into the database", newCurrencies.size());
        }
    }
}
