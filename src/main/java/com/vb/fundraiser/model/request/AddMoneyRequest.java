package com.vb.fundraiser.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddMoneyRequest(
    @NotBlank(message = "Currency code must not be blank")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    String currencyCode,

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than 0")
    BigDecimal amount
) {}