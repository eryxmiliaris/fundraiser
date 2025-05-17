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

    private CurrencyConversionClient client;

    private static final String BASE_URL = "https://api.unirateapi.com/api/convert";
    private static final String API_KEY = "TEST_KEY";
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("12.99");
    private static final String DEFAULT_FROM_CURRENCY = "EUR";
    private static final String DEFAULT_TO_CURRENCY = "USD";

    @BeforeEach
    void setUp() {
        client = new CurrencyConversionClient(mockRestTemplate, BASE_URL, API_KEY);
    }

    @Test
    void givenValidInput_whenConvert_thenReturnConvertedResult() {
        // given
        CurrencyConversionResponse resp = new CurrencyConversionResponse(
                DEFAULT_AMOUNT,
                DEFAULT_FROM_CURRENCY,
                DEFAULT_TO_CURRENCY,
                new BigDecimal("49.16947748517189")
        );

        String expectedUrl = BASE_URL +
                "?api_key=" + API_KEY +
                "&amount=" + DEFAULT_AMOUNT +
                "&from=" + DEFAULT_FROM_CURRENCY +
                "&to=" + DEFAULT_TO_CURRENCY;

        when(mockRestTemplate.getForObject(eq(expectedUrl), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when
        BigDecimal result = client.convert(DEFAULT_AMOUNT, DEFAULT_FROM_CURRENCY, DEFAULT_TO_CURRENCY);

        // then
        assertThat(result).isEqualByComparingTo(resp.result());
    }

    @Test
    void givenNullApiResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(null);

        // when / then
        assertThatThrownBy(() -> client.convert(DEFAULT_AMOUNT, DEFAULT_FROM_CURRENCY, DEFAULT_TO_CURRENCY))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + DEFAULT_AMOUNT + " from " + DEFAULT_FROM_CURRENCY + " to " + DEFAULT_TO_CURRENCY + " failed");
    }

    @Test
    void givenNullResultInResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        CurrencyConversionResponse resp = new CurrencyConversionResponse(DEFAULT_AMOUNT, DEFAULT_FROM_CURRENCY, DEFAULT_TO_CURRENCY, null);
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when / then
        assertThatThrownBy(() -> client.convert(DEFAULT_AMOUNT, DEFAULT_FROM_CURRENCY, DEFAULT_TO_CURRENCY))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + DEFAULT_AMOUNT + " from " + DEFAULT_FROM_CURRENCY + " to " + DEFAULT_TO_CURRENCY + " failed");
    }

    @Test
    void givenNullArguments_whenConvert_thenThrowIllegalArgumentException() {
        // when / then
        assertThatThrownBy(() -> client.convert(null, DEFAULT_FROM_CURRENCY, DEFAULT_TO_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");

        assertThatThrownBy(() -> client.convert(DEFAULT_AMOUNT, null, DEFAULT_TO_CURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From currencyCode must not be blank");

        assertThatThrownBy(() -> client.convert(DEFAULT_AMOUNT, DEFAULT_FROM_CURRENCY, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("To currencyCode must not be blank");
    }
}