package com.vb.fundraiser.init;

import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyInitializerTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyInitializer initializer;

    @Test
    void givenCurrenciesNotInDatabase_whenInitCurrencies_thenInsertCurrenciesFromIso() {
        // given
        when(currencyRepository.findByCode(anyString()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<Currency> captor = ArgumentCaptor.forClass(Currency.class);

        // when
        initializer.initCurrencies();

        // then
        int expectedCount = java.util.Currency.getAvailableCurrencies().size();
        verify(currencyRepository, times(expectedCount)).save(captor.capture());

        assertThat(captor.getAllValues()).hasSize(expectedCount);
    }

    @Test
    void givenAllCurrenciesExistInDatabase_whenInitCurrencies_thenSkipAllInserts() {
        // given
        when(currencyRepository.findByCode(anyString()))
                .thenReturn(Optional.of(Currency.builder().code("EXISTING").build()));

        // when
        initializer.initCurrencies();

        // then
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void givenSomeCurrenciesExist_whenInitCurrencies_thenSaveOnlyMissingOnes() {
        // given
        when(currencyRepository.findByCode(anyString()))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    if (Set.of("USD", "EUR").contains(code)) {
                        return Optional.of(Currency.builder().code(code).build());
                    }
                    return Optional.empty();
                });

        // when
        initializer.initCurrencies();

        // then
        verify(currencyRepository).save(argThat(c -> c.getCode().equals("PLN")));
        verify(currencyRepository).save(argThat(c -> c.getCode().equals("JPY")));
        verify(currencyRepository, never()).save(argThat(c -> c.getCode().equals("USD")));
        verify(currencyRepository, never()).save(argThat(c -> c.getCode().equals("EUR")));
    }
}