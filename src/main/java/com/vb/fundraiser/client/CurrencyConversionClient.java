package com.vb.fundraiser.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CurrencyConversionClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${currency.unirate.base-url}")
    private String baseUrl;

    @Value("${currency.unirate.api-key}")
    private String apiKey;

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
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