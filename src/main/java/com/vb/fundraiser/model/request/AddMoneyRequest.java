package com.vb.fundraiser.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AddMoneyRequest(
    @NotBlank(message = "Currency code must not be blank")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Schema(example = "USD")
    String currencyCode,

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than 0")
    BigDecimal amount
) {}