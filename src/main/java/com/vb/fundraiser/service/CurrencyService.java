package com.vb.fundraiser.service;

import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    @Cacheable("currencies")
    public List<String> getAllCurrencies() {
        var currencies = currencyRepository.findAll()
                .stream()
                .map(Currency::getCode)
                .toList();

        log.info("Fetched {} currencies", currencies.size());
        return currencies;
    }
}