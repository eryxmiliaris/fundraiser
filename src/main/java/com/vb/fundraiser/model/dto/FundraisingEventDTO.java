package com.vb.fundraiser.model.dto;

import java.math.BigDecimal;

public record FundraisingEventDTO(
        Long id,
        String name,
        String currencyCode,
        BigDecimal accountBalance
) {}