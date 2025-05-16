package com.vb.fundraiser.init;

import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.repository.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyInitializer {
    private final CurrencyRepository currencyRepository;

    @PostConstruct
    public void initCurrencies() {
        Set<String> javaCurrencies = java.util.Currency.getAvailableCurrencies().stream()
                .map(java.util.Currency::getCurrencyCode)
                .collect(Collectors.toSet());

        long added = javaCurrencies.stream()
                .filter(code -> currencyRepository.findByCode(code).isEmpty())
                .map(code -> Currency.builder().code(code).build())
                .peek(currencyRepository::save)
                .count();

        log.info("Initialized {} ISO fiat currencies into the database", added);
    }
}