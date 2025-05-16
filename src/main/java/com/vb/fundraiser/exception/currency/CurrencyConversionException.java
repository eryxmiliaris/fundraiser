package com.vb.fundraiser.exception.currency;

import java.math.BigDecimal;

public class CurrencyConversionException extends RuntimeException {
    public CurrencyConversionException(BigDecimal amount, String fromCurrency, String toCurrency) {
        super("Currency conversion for amount " + amount + " from " + fromCurrency + " to " + toCurrency + " failed");
    }
}
