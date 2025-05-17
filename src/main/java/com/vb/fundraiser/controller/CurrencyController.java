package com.vb.fundraiser.controller;

import com.vb.fundraiser.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Currencies", description = "Endpoints for retrieving available currencies")
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @Operation(summary = "Get all available currencies")
    @GetMapping
    public ResponseEntity<List<String>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }
}
