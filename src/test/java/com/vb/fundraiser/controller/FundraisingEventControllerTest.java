package com.vb.fundraiser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.service.FundraisingEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

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

    private static final String DEFAULT_EVENT_NAME = "Charity";
    private static final String DEFAULT_CURRENCY_CODE = "EUR";
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.ZERO;

    private FundraisingEventDTO eventDto;

    @BeforeEach
    void setUp() {
        eventDto = new FundraisingEventDTO(1L, DEFAULT_EVENT_NAME, DEFAULT_CURRENCY_CODE, DEFAULT_AMOUNT);
    }

    @Test
    void givenValidRequest_whenCreateEvent_thenReturnEventDTO() throws Exception {
        // given
        CreateEventRequest request = new CreateEventRequest(DEFAULT_EVENT_NAME, DEFAULT_CURRENCY_CODE);
        when(eventService.createEvent(Mockito.any())).thenReturn(eventDto);

        // when / then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(DEFAULT_EVENT_NAME))
                .andExpect(jsonPath("$.currencyCode").value(DEFAULT_CURRENCY_CODE))
                .andExpect(jsonPath("$.accountBalance").value(DEFAULT_AMOUNT));
    }

    @Test
    void givenInvalidCurrency_whenCreateEvent_thenThrowCurrencyNotFoundException() throws Exception {
        // given
        String invalidCurrencyCode = "ZZZ";
        CreateEventRequest request = new CreateEventRequest(DEFAULT_EVENT_NAME, invalidCurrencyCode);
        when(eventService.createEvent(Mockito.any()))
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
        CreateEventRequest request = new CreateEventRequest("", DEFAULT_CURRENCY_CODE);

        // when / then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenBlankCurrency_whenCreateEvent_thenReturnBadRequest() throws Exception {
        // given
        CreateEventRequest request = new CreateEventRequest(DEFAULT_EVENT_NAME, "");

        // when / then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidCurrencyFormat_whenCreateEvent_thenReturnBadRequest() throws Exception {
        // given
        CreateEventRequest request = new CreateEventRequest(DEFAULT_EVENT_NAME, "eur");

        // when / then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenOneEventExists_whenGetFinancialReport_thenReturnSingleEntry() throws Exception {
        // given
        when(eventService.getFinancialReport()).thenReturn(List.of(eventDto));

        // when / then
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(DEFAULT_EVENT_NAME))
                .andExpect(jsonPath("$[0].accountBalance").value(DEFAULT_AMOUNT))
                .andExpect(jsonPath("$[0].currencyCode").value(DEFAULT_CURRENCY_CODE));
    }

    @Test
    void givenNoEventsExist_whenGetFinancialReport_thenReturnEmptyList() throws Exception {
        // given
        when(eventService.getFinancialReport()).thenReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
