package com.vb.fundraiser.exception.currency;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String code) {
        super("Currency '" + code + "' not found or unsupported");
    }
}
