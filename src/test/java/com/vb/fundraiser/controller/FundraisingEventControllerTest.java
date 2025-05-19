package com.vb.fundraiser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.service.FundraisingEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FundraisingEventController.class)
class FundraisingEventControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FundraisingEventService eventService;

    private static final String EVENT_NAME = "Charity";
    private static final String CURRENCY_CODE = "EUR";
    private static final BigDecimal AMOUNT = BigDecimal.ZERO;

    private FundraisingEventDTO eventDto;

    @BeforeEach
    void setUp() {
        eventDto = new FundraisingEventDTO(1L, EVENT_NAME, CURRENCY_CODE, AMOUNT);
    }

    @Nested
    class CreateEvent {
        @Test
        void givenValidRequest_whenCreateEvent_thenReturnEventDTO() throws Exception {
            // given
            CreateEventRequest request = new CreateEventRequest(EVENT_NAME, CURRENCY_CODE);
            when(eventService.createEvent(EVENT_NAME, CURRENCY_CODE)).thenReturn(eventDto);

            // when / then
            mockMvc.perform(post("/api/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value(EVENT_NAME))
                    .andExpect(jsonPath("$.currencyCode").value(CURRENCY_CODE))
                    .andExpect(jsonPath("$.accountBalance").value(AMOUNT));
        }

        @Test
        void givenInvalidCurrency_whenCreateEvent_thenThrowCurrencyNotFoundException() throws Exception {
            // given
            String invalidCurrencyCode = "ZZZ";
            CreateEventRequest request = new CreateEventRequest(EVENT_NAME, invalidCurrencyCode);
            when(eventService.createEvent(EVENT_NAME, invalidCurrencyCode))
                    .thenThrow(new CurrencyNotFoundException(invalidCurrencyCode));

            // when / then
            mockMvc.perform(post("/api/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Currency '" + invalidCurrencyCode + "' not found or unsupported"));
        }

        @Test
        void givenBlankName_whenCreateEvent_thenReturnBadRequest() throws Exception {
            // given
            CreateEventRequest request = new CreateEventRequest("", CURRENCY_CODE);

            // when / then
            mockMvc.perform(post("/api/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenBlankCurrency_whenCreateEvent_thenReturnBadRequest() throws Exception {
            // given
            CreateEventRequest request = new CreateEventRequest(EVENT_NAME, "");

            // when / then
            mockMvc.perform(post("/api/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetFinancialReport {
        @Test
        void givenOneEventExists_whenGetFinancialReport_thenReturnPageWithSingleEntry() throws Exception {
            // given
            Page<FundraisingEventDTO> page = new PageImpl<>(List.of(eventDto));
            when(eventService.getFinancialReport(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

            // when / then
            mockMvc.perform(get("/api/v1/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value(EVENT_NAME))
                    .andExpect(jsonPath("$.content[0].accountBalance").value(AMOUNT))
                    .andExpect(jsonPath("$.content[0].currencyCode").value(CURRENCY_CODE));
        }

        @Test
        void givenNoEventsExist_whenGetFinancialReport_thenReturnEmptyPage() throws Exception {
            // given
            Page<FundraisingEventDTO> page = new PageImpl<>(List.of());
            when(eventService.getFinancialReport(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

            // when / then
            mockMvc.perform(get("/api/v1/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    class GenerateHtmlReport {
        @Test
        void givenDefaultParameters_whenGenerateHtmlReport_thenReturnHtml() throws Exception {
            // when
            when(eventService.generateHtmlReport("name", "asc")).thenReturn("<html>ok</html>");

            // then
            mockMvc.perform(get("/api/v1/events/table"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<html>")));
        }
    }
}
