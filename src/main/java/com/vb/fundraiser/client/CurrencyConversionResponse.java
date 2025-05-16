package com.vb.fundraiser.client;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencyConversionResponse {
    private BigDecimal amount;
    private String from;
    private String to;
    private BigDecimal result;
}
