package com.vb.fundraiser.client;

import com.vb.fundraiser.exception.currency.CurrencyConversionException;
import com.vb.fundraiser.model.response.CurrencyConversionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionClientTest {
    @Mock
    private RestTemplate mockRestTemplate;
    private static final String BASE_URL = "https://api.unirateapi.com/api/convert";
    private static final String API_KEY = "TEST_KEY";

    private CurrencyConversionClient client;

    @BeforeEach
    void setUp() {
        client = new CurrencyConversionClient(mockRestTemplate, BASE_URL, API_KEY);
    }

    @Test
    void givenValidInput_whenConvert_thenReturnConvertedResult() {
        // given
        BigDecimal amount = new BigDecimal("12.99");
        String from = "USD";
        String to = "PLN";

        CurrencyConversionResponse resp = new CurrencyConversionResponse(
                amount,
                from,
                to,
                new BigDecimal("49.16947748517189")
        );

        String expectedUrl = BASE_URL +
                "?api_key=" + API_KEY +
                "&amount=" + amount +
                "&from=" + from +
                "&to=" + to;

        when(mockRestTemplate.getForObject(eq(expectedUrl), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when
        BigDecimal result = client.convert(amount, from, to);

        // then
        assertThat(result).isEqualByComparingTo(resp.result());
    }

    @Test
    void givenNullApiResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        BigDecimal amount = new BigDecimal("12.99");
        String from = "USD";
        String to = "PLN";
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(null);

        // when / then
        assertThatThrownBy(() -> client.convert(amount, from, to))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + amount + " from " + from + " to " + to + " failed");
    }

    @Test
    void givenNullResultInResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        BigDecimal amount = BigDecimal.ONE;
        String from = "EUR";
        String to = "GBP";
        CurrencyConversionResponse resp = new CurrencyConversionResponse(amount, from, to, null);
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when / then
        assertThatThrownBy(() -> client.convert(amount, from, to))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + amount + " from " + from + " to " + to + " failed");
    }

    @Test
    void givenNullArguments_whenConvert_thenThrowIllegalArgumentException() {
        // when / then
        assertThatThrownBy(() -> client.convert(null, "USD", "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");

        assertThatThrownBy(() -> client.convert(BigDecimal.TEN, null, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From currency must not be blank");

        assertThatThrownBy(() -> client.convert(BigDecimal.TEN, "USD", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("To currency must not be blank");
    }
}