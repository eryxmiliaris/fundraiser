package com.vb.fundraiser.model.response;

import java.math.BigDecimal;

public record CurrencyConversionResponse(
        BigDecimal amount,
        String from,
        String to,
        BigDecimal result
) {}
