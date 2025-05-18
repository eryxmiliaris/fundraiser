package com.vb.fundraiser.client;

import com.vb.fundraiser.exception.currency.CurrencyConversionException;
import com.vb.fundraiser.client.response.CurrencyConversionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Slf4j
@Component
public class CurrencyConversionClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public CurrencyConversionClient(
            RestTemplate restTemplate,
            @Value("${currency.unirate.base-url}") String baseUrl,
            @Value("${currency.unirate.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        validateInput(amount, fromCurrency, toCurrency);
        log.info("Converting amount {} from {} to {}", amount, fromCurrency, toCurrency);

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("api_key", apiKey)
                .queryParam("amount", amount)
                .queryParam("from", fromCurrency)
                .queryParam("to", toCurrency)
                .toUriString();

        CurrencyConversionResponse response = restTemplate.getForObject(url, CurrencyConversionResponse.class);
        if (response == null || response.result() == null) {
            log.error("Currency conversion failed for amount {} from {} to {}", amount, fromCurrency, toCurrency);
            throw new CurrencyConversionException(amount, fromCurrency, toCurrency);
        }

        log.info("Currency converted result: {}", response.result());
        return response.result();
    }

    private void validateInput(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (!StringUtils.hasText(fromCurrency)) {
            throw new IllegalArgumentException("From currency must not be blank");
        }
        if (!StringUtils.hasText(toCurrency)) {
            throw new IllegalArgumentException("To currency must not be blank");
        }
    }
}