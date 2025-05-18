package com.vb.fundraiser.service;

import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FundraisingEventServiceTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private FundraisingEventRepository eventRepository;

    @InjectMocks
    private FundraisingEventService eventService;

    private static final String EVENT_NAME = "Charity";
    private static final String CURRENCY_CODE = "EUR";
    private static final BigDecimal ZERO_BALANCE = BigDecimal.ZERO;

    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = Currency.builder().id(1L).code("EUR").build();
    }

    @Test
    void givenValidRequest_whenCreateEvent_thenReturnEventDTO() {
        // given
        CreateEventRequest request = new CreateEventRequest(EVENT_NAME, CURRENCY_CODE);
        FundraisingEvent saved = FundraisingEvent.builder()
                .id(1L)
                .name(EVENT_NAME)
                .currency(currency)
                .accountBalance(ZERO_BALANCE)
                .build();

        when(currencyRepository.findByCode(CURRENCY_CODE)).thenReturn(Optional.of(currency));
        when(eventRepository.save(any())).thenReturn(saved);

        // when
        FundraisingEventDTO result = eventService.createEvent(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo(EVENT_NAME);
        assertThat(result.currencyCode()).isEqualTo(CURRENCY_CODE);
        assertThat(result.accountBalance()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void givenInvalidCurrency_whenCreateEvent_thenThrowCurrencyNotFoundException() {
        // given
        String invalidCurrencyCode = "ZZZ";
        CreateEventRequest request = new CreateEventRequest(EVENT_NAME, invalidCurrencyCode);
        when(currencyRepository.findByCode(invalidCurrencyCode)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessage("Currency '" + invalidCurrencyCode + "' not found or unsupported");
    }

    @Test
    void givenSingleEvent_whenGetFinancialReport_thenReturnRoundedResult() {
        // given
        FundraisingEvent event = FundraisingEvent.builder()
                .id(1L)
                .name(EVENT_NAME)
                .currency(currency)
                .accountBalance(new BigDecimal("123.45678"))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(event));

        // when
        List<FundraisingEventDTO> result = eventService.getFinancialReport();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo(EVENT_NAME);
        assertThat(result.getFirst().currencyCode()).isEqualTo(CURRENCY_CODE);
        assertThat(result.getFirst().accountBalance()).isEqualByComparingTo(new BigDecimal("123.46"));
    }

    @Test
    void givenMultipleEvents_whenGetFinancialReport_thenReturnAll() {
        // given
        FundraisingEvent e1 = FundraisingEvent.builder()
                .name("One")
                .currency(currency)
                .accountBalance(new BigDecimal("50.00"))
                .build();
        FundraisingEvent e2 = FundraisingEvent.builder()
                .name("Two")
                .currency(currency)
                .accountBalance(new BigDecimal("75.00"))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(e1, e2));

        // when
        List<FundraisingEventDTO> result = eventService.getFinancialReport();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FundraisingEventDTO::name)
                .containsExactlyInAnyOrder("One", "Two");
    }
}