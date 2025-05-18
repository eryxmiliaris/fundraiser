package com.vb.fundraiser.init;

import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyInitializerTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyInitializer initializer;

    private final Set<String> isoCodes = java.util.Currency.getAvailableCurrencies().stream()
            .map(java.util.Currency::getCurrencyCode)
            .collect(Collectors.toSet());

    @Test
    void givenNoCurrenciesInDatabase_whenInitCurrencies_thenInsertAllJavaCurrencies() {
        // given
        when(currencyRepository.findAll()).thenReturn(List.of());

        ArgumentCaptor<List<Currency>> captor = ArgumentCaptor.forClass(List.class);

        // when
        initializer.initCurrencies();

        // then
        int expectedCount = isoCodes.size();

        verify(currencyRepository).saveAll(captor.capture());
        List<Currency> saved = captor.getValue();

        assertThat(saved).hasSize(expectedCount);
    }

    @Test
    void givenAllCurrenciesExistInDatabase_whenInitCurrencies_thenSkipAllInserts() {
        // given
        List<Currency> existing = isoCodes.stream()
                .map(code -> Currency.builder().code(code).build())
                .toList();

        when(currencyRepository.findAll()).thenReturn(existing);

        // when
        initializer.initCurrencies();

        // then
        verify(currencyRepository, never()).saveAll(any());
    }

    @Test
    void givenSomeCurrenciesExist_whenInitCurrencies_thenSaveOnlyMissingOnes() {
        // given
        Currency usd = Currency.builder().code("USD").build();
        Currency eur = Currency.builder().code("EUR").build();
        when(currencyRepository.findAll()).thenReturn(List.of(usd, eur));

        ArgumentCaptor<List<Currency>> captor = ArgumentCaptor.forClass(List.class);

        // when
        initializer.initCurrencies();

        // then
        verify(currencyRepository).saveAll(captor.capture());
        List<Currency> saved = captor.getValue();

        assertThat(saved).allMatch(c -> !List.of("USD", "EUR").contains(c.getCode()));
    }
}