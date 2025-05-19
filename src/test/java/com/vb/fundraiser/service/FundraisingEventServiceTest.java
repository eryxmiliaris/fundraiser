package com.vb.fundraiser.service;

import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.exception.event.FundraisingEventAlreadyExistsException;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private static final String DIRECTION = "asc";

    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = Currency.builder().id(1L).code("EUR").build();
    }

    @Nested
    class CreateEvent {
        @Test
        void givenValidRequest_whenCreateEvent_thenReturnEventDTO() {
            // given
            FundraisingEvent saved = FundraisingEvent.builder()
                    .id(1L)
                    .name(EVENT_NAME)
                    .currency(currency)
                    .accountBalance(ZERO_BALANCE)
                    .build();

            when(currencyRepository.findByCode(CURRENCY_CODE)).thenReturn(Optional.of(currency));
            when(eventRepository.save(any())).thenReturn(saved);

            // when
            FundraisingEventDTO result = eventService.createEvent(EVENT_NAME, CURRENCY_CODE);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo(EVENT_NAME);
            assertThat(result.currencyCode()).isEqualTo(CURRENCY_CODE);
            assertThat(result.accountBalance()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        void givenDuplicateEventName_whenCreateEvent_thenThrowFundraisingEventAlreadyExistsException() {
            // given
            when(eventRepository.existsByName(EVENT_NAME)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> eventService.createEvent(EVENT_NAME, CURRENCY_CODE))
                    .isInstanceOf(FundraisingEventAlreadyExistsException.class)
                    .hasMessage("Fundraising event with name '" + EVENT_NAME + "' already exists");

            verifyNoInteractions(currencyRepository);

            verifyNoMoreInteractions(eventRepository);
        }

        @Test
        void givenInvalidCurrency_whenCreateEvent_thenThrowCurrencyNotFoundException() {
            // given
            String invalidCurrencyCode = "ZZZ";
            when(currencyRepository.findByCode(invalidCurrencyCode)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> eventService.createEvent(EVENT_NAME, invalidCurrencyCode))
                    .isInstanceOf(CurrencyNotFoundException.class)
                    .hasMessage("Currency '" + invalidCurrencyCode + "' not found or unsupported");
        }
    }

    @Nested
    class GetFinancialReport {
        @Test
        void givenSingleEvent_whenGetFinancialReport_thenReturnRoundedAndPagedResult() {
            // given
            FundraisingEvent event = FundraisingEvent.builder()
                    .id(1L)
                    .name(EVENT_NAME)
                    .currency(currency)
                    .accountBalance(new BigDecimal("123.45678"))
                    .build();

            Page<FundraisingEvent> page = new PageImpl<>(List.of(event));

            when(eventRepository.findAll(any(Pageable.class))).thenReturn(page);

            // when
            Page<FundraisingEventDTO> result = eventService.getFinancialReport(0, 10, "name", DIRECTION);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            FundraisingEventDTO dto = result.getContent().getFirst();
            assertThat(dto.name()).isEqualTo(EVENT_NAME);
            assertThat(dto.accountBalance()).isEqualByComparingTo(new BigDecimal("123.46"));
        }

        @Test
        void givenValidPagination_whenGetFinancialReport_thenReturnPageOfEvents() {
            // given
            FundraisingEvent event = FundraisingEvent.builder()
                    .id(1L)
                    .name("Save Animals")
                    .currency(currency)
                    .accountBalance(new BigDecimal("10.125"))
                    .build();

            Page<FundraisingEvent> eventPage = new PageImpl<>(List.of(event));
            when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);

            // when
            Page<FundraisingEventDTO> result = eventService.getFinancialReport(0, 5, "name", "asc");

            // then
            assertThat(result).hasSize(1);
            FundraisingEventDTO dto = result.getContent().getFirst();
            assertThat(dto.name()).isEqualTo("Save Animals");
            assertThat(dto.accountBalance()).isEqualByComparingTo("10.13");
        }

        @Test
        void givenInvalidPageNumber_whenGetFinancialReport_thenThrowException() {
            // when / then
            assertThatThrownBy(() -> eventService.getFinancialReport(-1, 10, "name", "asc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page index must not be negative");
        }

        @Test
        void givenInvalidPageSize_whenGetFinancialReport_thenThrowException() {
            // when / then
            assertThatThrownBy(() -> eventService.getFinancialReport(0, 0, "name", "asc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must be between 1 and 100");
        }

        @Test
        void givenInvalidDirection_whenGetFinancialReport_thenThrowException() {
            // when / then
            assertThatThrownBy(() -> eventService.getFinancialReport(0, 10, "name", "ascending"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sort direction must be 'asc' or 'desc'");
        }
    }

    @Nested
    class GenerateHtmlReport {
        @Test
        void givenValidSortField_whenGenerateHtmlReport_thenReturnHtml() {
            // given
            FundraisingEvent event = FundraisingEvent.builder()
                    .id(1L)
                    .name(EVENT_NAME)
                    .currency(currency)
                    .accountBalance(BigDecimal.ZERO)
                    .build();

            when(eventRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(event)));

            // when
            String html = eventService.generateHtmlReport("name", "asc");

            // then
            assertThat(html).contains("<table>").contains(EVENT_NAME).contains("</table>");
        }

        @Test
        void givenNoEvents_whenGenerateHtmlReport_thenReturnsHtmlWithHeaderOnly() {
            // given
            when(eventRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            // when
            String html = eventService.generateHtmlReport("name", "asc");

            // then
            assertThat(html).contains("<h1>Fundraising Events Report</h1>");
            assertThat(html).contains("<th>Fundraising event name</th>");
            assertThat(html).contains("<table>");
            assertThat(html).doesNotContain("<td>");
        }
    }
}