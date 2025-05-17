package com.vb.fundraiser.model.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AddMoneyRequest(
    @NotBlank(message = "Currency code must not be blank")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    String currencyCode,

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than 0")
    BigDecimal amount
) {}