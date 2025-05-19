package com.vb.fundraiser.client;

import com.vb.fundraiser.exception.currency.CurrencyConversionException;
import com.vb.fundraiser.client.response.CurrencyConversionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
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

    private static final BigDecimal AMOUNT = new BigDecimal("12.99");
    private static final String FROM = "EUR";
    private static final String TO = "USD";

    @BeforeEach
    void setUp() {
        client = new CurrencyConversionClient(mockRestTemplate, BASE_URL, API_KEY);
    }

    @Test
    void givenValidInput_whenConvert_thenReturnConvertedResult() {
        // given
        CurrencyConversionResponse resp = new CurrencyConversionResponse(
                AMOUNT,
                FROM,
                TO,
                new BigDecimal("49.16947748517189")
        );

        String expectedUrl = BASE_URL +
                "?api_key=" + API_KEY +
                "&amount=" + AMOUNT +
                "&from=" + FROM +
                "&to=" + TO;

        when(mockRestTemplate.getForObject(eq(expectedUrl), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when
        BigDecimal result = client.convert(AMOUNT, FROM, TO);

        // then
        assertThat(result).isEqualByComparingTo(resp.result());
    }

    @Test
    void givenNullApiResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(null);

        // when / then
        assertThatThrownBy(() -> client.convert(AMOUNT, FROM, TO))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + AMOUNT + " from " + FROM + " to " + TO + " failed");
    }

    @Test
    void givenNullResultInResponse_whenConvert_thenThrowCurrencyConversionException() {
        // given
        CurrencyConversionResponse resp = new CurrencyConversionResponse(AMOUNT, FROM, TO, null);
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenReturn(resp);

        // when / then
        assertThatThrownBy(() -> client.convert(AMOUNT, FROM, TO))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessage("Currency conversion for amount " + AMOUNT + " from " + FROM + " to " + TO + " failed");
    }

    @Test
    void givenUnauthorizedFromApi_whenConvert_thenThrowUnauthorizedHandledByControllerAdvice() {
        // given
        when(mockRestTemplate.getForObject(anyString(), eq(CurrencyConversionResponse.class)))
                .thenThrow(HttpClientErrorException.create("", HttpStatusCode.valueOf(401), null, null, null, null));

        // when / then
        assertThatThrownBy(() ->
                client.convert(BigDecimal.TEN, "USD", "EUR"))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void givenNullArguments_whenConvert_thenThrowIllegalArgumentException() {
        // when / then
        assertThatThrownBy(() -> client.convert(null, FROM, TO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");

        assertThatThrownBy(() -> client.convert(AMOUNT, null, TO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From currency must not be blank");

        assertThatThrownBy(() -> client.convert(AMOUNT, FROM, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("To currency must not be blank");
    }
}