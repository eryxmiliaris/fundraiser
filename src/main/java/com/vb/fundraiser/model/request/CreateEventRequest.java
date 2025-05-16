package com.vb.fundraiser.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank(message = "Event name must not be blank")
        @Size(max = 255, message = "Event name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Currency code must not be blank")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
        String currencyCode
) {}
