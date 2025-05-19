package com.vb.fundraiser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vb.fundraiser.exception.box.*;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.exception.currency.InvalidMoneyAmountException;
import com.vb.fundraiser.exception.event.FundraisingEventNotFoundException;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.model.request.AddMoneyRequest;
import com.vb.fundraiser.service.CollectionBoxService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionBoxController.class)
class CollectionBoxControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CollectionBoxService boxService;

    private static final Long BOX_ID = 1L;
    private static final Long EVENT_ID = 100L;

    @Nested
    class RegisterBox {
        @Test
        void givenBoxRegistered_whenRegisterNewBox_thenReturnDTO() throws Exception {
            // given
            CollectionBoxDTO dto = new CollectionBoxDTO(BOX_ID, false, true);
            when(boxService.registerNewBox()).thenReturn(dto);

            // when / then
            mockMvc.perform(post("/api/v1/boxes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BOX_ID))
                    .andExpect(jsonPath("$.assigned").value(false))
                    .andExpect(jsonPath("$.empty").value(true));
        }
    }

    @Nested
    class GetBoxes {
        @Test
        void givenBoxesExist_whenGetAllBoxes_thenReturnPagedResult() throws Exception {
            // given
            List<CollectionBoxDTO> boxes = List.of(
                    new CollectionBoxDTO(1L, false, true),
                    new CollectionBoxDTO(2L, true, false)
            );
            Page<CollectionBoxDTO> page = new PageImpl<>(boxes);
            when(boxService.getAllBoxes(0, 10, "asc")).thenReturn(page);

            // when / then
            mockMvc.perform(get("/api/v1/boxes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        void givenNoBoxesExist_whenGetAllBoxes_thenReturnEmptyPage() throws Exception {
            // given
            when(boxService.getAllBoxes(0, 10, "asc")).thenReturn(Page.empty());

            // when / then
            mockMvc.perform(get("/api/v1/boxes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    class UnregisterBox {
        @Test
        void givenBoxExists_whenUnregisterBox_thenReturnSuccessMessage() throws Exception {
            // when / then
            mockMvc.perform(delete("/api/v1/boxes/{boxId}", BOX_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Box " + BOX_ID + " successfully unregistered")));
        }

        @Test
        void givenBoxNotFound_whenUnregisterBox_thenReturnNotFound() throws Exception {
            // given
            doThrow(new BoxNotFoundException(BOX_ID)).when(boxService).unregisterBox(BOX_ID);

            // when / then
            mockMvc.perform(delete("/api/v1/boxes/{boxId}", BOX_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Collection box with ID " + BOX_ID + " not found"));
        }
    }

    @Nested
    class AssignBoxToEvent {
        @Test
        void givenValidBoxAndEvent_whenAssignBoxToEvent_thenReturnDTO() throws Exception {
            // given
            CollectionBoxDTO dto = new CollectionBoxDTO(BOX_ID, true, false);
            when(boxService.assignBoxToEvent(BOX_ID, EVENT_ID)).thenReturn(dto);

            // when / then
            mockMvc.perform(patch("/api/v1/boxes/{boxId}/assign?eventId={eventId}", BOX_ID, EVENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assigned").value(true))
                    .andExpect(jsonPath("$.empty").value(false));
        }

        @Test
        void givenBoxAlreadyAssigned_whenAssignBoxToEvent_thenReturnBadRequest() throws Exception {
            // given
            when(boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .thenThrow(new BoxAlreadyAssignedException(BOX_ID));

            // when / then
            mockMvc.perform(patch("/api/v1/boxes/{boxId}/assign?eventId={eventId}", BOX_ID, EVENT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Box " + BOX_ID + " is already assigned to a fundraising event"));
        }

        @Test
        void givenBoxNotEmpty_whenAssignBoxToEvent_thenReturnBadRequest() throws Exception {
            // given
            when(boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .thenThrow(new NotEmptyBoxAssignmentException(BOX_ID));

            // when / then
            mockMvc.perform(patch("/api/v1/boxes/{boxId}/assign?eventId={eventId}", BOX_ID, EVENT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cannot assign box " + BOX_ID + " because it is not empty"));
        }

        @Test
        void givenEventNotFound_whenAssignBoxToEvent_thenReturnNotFound() throws Exception {
            // given
            when(boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .thenThrow(new FundraisingEventNotFoundException(EVENT_ID));

            // when / then
            mockMvc.perform(patch("/api/v1/boxes/{boxId}/assign?eventId={eventId}", BOX_ID, EVENT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Fundraising event with ID " + EVENT_ID + " not found"));
        }
    }

    @Nested
    class AddMoney {
        @Test
        void givenValidRequest_whenAddMoney_thenReturnSuccessMessage() throws Exception {
            // given
            AddMoneyRequest request = new AddMoneyRequest("USD", BigDecimal.valueOf(10));

            // when / then
            mockMvc.perform(put("/api/v1/boxes/{id}/add-money", BOX_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Money successfully added to the box " + BOX_ID)));
        }

        @Test
        void givenInvalidAmount_whenAddMoney_thenReturnBadRequest() throws Exception {
            // given
            AddMoneyRequest request = new AddMoneyRequest("EUR", BigDecimal.ZERO);
            doThrow(new InvalidMoneyAmountException(BigDecimal.ZERO))
                    .when(boxService).addMoney(eq(BOX_ID), eq("EUR"), eq(BigDecimal.ZERO));

            // when / then
            mockMvc.perform(put("/api/v1/boxes/{id}/add-money", BOX_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void givenInvalidCurrency_whenAddMoney_thenReturnNotFound() throws Exception {
            // given
            String invalidCurrencyCode = "ZZZ";
            AddMoneyRequest request = new AddMoneyRequest(invalidCurrencyCode, BigDecimal.TEN);
            doThrow(new CurrencyNotFoundException(invalidCurrencyCode))
                    .when(boxService).addMoney(eq(BOX_ID), eq(invalidCurrencyCode), eq(BigDecimal.TEN));

            // when / then
            mockMvc.perform(put("/api/v1/boxes/{id}/add-money", BOX_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Currency '" + invalidCurrencyCode + "' not found or unsupported"));
        }
    }

    @Nested
    class EmptyBox {
        @Test
        void givenBoxIsValid_whenEmptyBox_thenReturnSuccessMessage() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/boxes/{id}/empty", BOX_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Box " + BOX_ID + " successfully emptied")));
        }

        @Test
        void givenBoxNotAssigned_whenEmptyBox_thenReturnBadRequest() throws Exception {
            // given
            doThrow(new BoxNotAssignedException(BOX_ID))
                    .when(boxService).emptyBox(BOX_ID);

            // when / then
            mockMvc.perform(post("/api/v1/boxes/{id}/empty", BOX_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
