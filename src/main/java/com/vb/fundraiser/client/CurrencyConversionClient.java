package com.vb.fundraiser.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

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
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (!StringUtils.hasText(fromCurrency)) {
            throw new IllegalArgumentException("From currency must not be blank");
        }
        if (!StringUtils.hasText(toCurrency)) {
            throw new IllegalArgumentException("To currency must not be blank");
        }

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("api_key", apiKey)
                .queryParam("amount", amount)
                .queryParam("from", fromCurrency)
                .queryParam("to", toCurrency)
                .toUriString();

        CurrencyConversionResponse response = restTemplate.getForObject(url, CurrencyConversionResponse.class);
        if (response == null || response.getResult() == null) {
            throw new RuntimeException("Currency conversion failed");
        }

        return response.getResult();
    }
}