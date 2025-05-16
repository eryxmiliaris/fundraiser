package com.vb.fundraiser.model.dto;

import java.math.BigDecimal;

public record FinancialReportEntryDTO(
        String eventName,
        BigDecimal amount,
        String currencyCode
) {}