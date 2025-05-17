package com.vb.fundraiser.service;

import com.vb.fundraiser.client.CurrencyConversionClient;
import com.vb.fundraiser.exception.box.*;
import com.vb.fundraiser.exception.currency.CurrencyNotFoundException;
import com.vb.fundraiser.exception.currency.InvalidMoneyAmountException;
import com.vb.fundraiser.exception.event.FundraisingEventNotFoundException;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.model.entity.BoxCurrencyAmount;
import com.vb.fundraiser.model.entity.CollectionBox;
import com.vb.fundraiser.model.entity.Currency;
import com.vb.fundraiser.model.entity.FundraisingEvent;
import com.vb.fundraiser.repository.CollectionBoxRepository;
import com.vb.fundraiser.repository.CurrencyRepository;
import com.vb.fundraiser.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionBoxServiceTest {
    @Mock
    private CollectionBoxRepository boxRepository;
    @Mock
    private FundraisingEventRepository eventRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private CurrencyConversionClient conversionClient;

    @InjectMocks
    private CollectionBoxService boxService;

    @BeforeEach
    void setUp() {
        // No shared setup required for now
    }

    @Test
    void givenNoArguments_whenRegisterNewBox_thenReturnDtoWithIdAndDefaults() {
        // given
        CollectionBox savedBox = CollectionBox.builder().id(1L).event(null).build();
        when(boxRepository.save(any())).thenReturn(savedBox);

        // when
        CollectionBoxDTO result = boxService.registerNewBox();

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.assigned()).isFalse();
        assertThat(result.empty()).isTrue();
    }

    @Test
    void givenUnassignedEmptyBoxAndValidEvent_whenAssignBoxToEvent_thenReturnAssignedDto() {
        // given
        Long boxId = 1L;
        Long eventId = 10L;
        CollectionBox box = CollectionBox.builder().id(boxId).event(null).build();
        FundraisingEvent event = FundraisingEvent.builder().id(eventId).name("Charity Event").build();

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(boxRepository.save(any())).thenReturn(box);

        // when
        CollectionBoxDTO result = boxService.assignBoxToEvent(boxId, eventId);

        // then
        assertThat(result.id()).isEqualTo(boxId);
        assertThat(result.assigned()).isTrue();
        assertThat(result.empty()).isFalse();
    }

    @Test
    void givenNonExistingBox_whenAssignBoxToEvent_thenThrowBoxNotFoundException() {
        // given
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.assignBoxToEvent(1L, 2L))
                .isInstanceOf(BoxNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void givenBoxAlreadyAssigned_whenAssignBoxToEvent_thenThrowBoxAlreadyAssignedException() {
        // given
        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(FundraisingEvent.builder().id(2L).build())
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when / then
        assertThatThrownBy(() -> boxService.assignBoxToEvent(1L, 2L))
                .isInstanceOf(BoxAlreadyAssignedException.class);
    }

    @Test
    void givenBoxNotEmpty_whenAssignBoxToEvent_thenThrowNotEmptyBoxAssignmentException() {
        // given
        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(null)
                .amounts(List.of(BoxCurrencyAmount.builder()
                        .id(1L)
                        .amount(BigDecimal.TEN)
                        .currency(Currency.builder().id(1L).code("USD").build())
                        .build()))
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when / then
        assertThatThrownBy(() -> boxService.assignBoxToEvent(1L, 2L))
                .isInstanceOf(NotEmptyBoxAssignmentException.class);
    }

    @Test
    void givenEventDoesNotExist_whenAssignBoxToEvent_thenThrowFundraisingEventNotFoundException() {
        // given
        CollectionBox box = CollectionBox.builder().id(1L).event(null).build();
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.assignBoxToEvent(1L, 2L))
                .isInstanceOf(FundraisingEventNotFoundException.class);
    }

    @Test
    void givenNoBoxesExist_whenGetAllBoxes_thenReturnEmptyList() {
        // given
        when(boxRepository.findAll()).thenReturn(List.of());

        // when
        List<CollectionBoxDTO> result = boxService.getAllBoxes();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenBoxesWithDifferentStates_whenGetAllBoxes_thenReturnCorrectDTOs() {
        // given
        CollectionBox emptyBox = CollectionBox.builder()
                .id(1L)
                .event(null)
                .amounts(List.of())
                .build();

        CollectionBox assignedEmptyBox = CollectionBox.builder()
                .id(2L)
                .event(FundraisingEvent.builder().id(10L).build())
                .amounts(List.of())
                .build();

        CollectionBox nonEmptyUnassignedBox = CollectionBox.builder()
                .id(3L)
                .event(null)
                .amounts(List.of(
                        BoxCurrencyAmount.builder().amount(BigDecimal.TEN).build()
                ))
                .build();

        when(boxRepository.findAll()).thenReturn(List.of(emptyBox, assignedEmptyBox, nonEmptyUnassignedBox));

        // when
        List<CollectionBoxDTO> result = boxService.getAllBoxes();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).anyMatch(dto -> dto.id().equals(1L) && !dto.assigned() && dto.empty());
        assertThat(result).anyMatch(dto -> dto.id().equals(2L) && dto.assigned() && dto.empty());
        assertThat(result).anyMatch(dto -> dto.id().equals(3L) && !dto.assigned() && !dto.empty());
    }

    @Test
    void givenExistingBox_whenUnregisterBox_thenDeleteIt() {
        // given
        CollectionBox box = CollectionBox.builder().id(1L).build();
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when
        boxService.unregisterBox(1L);

        // then
        verify(boxRepository).delete(box);
    }

    @Test
    void givenMissingBox_whenUnregisterBox_thenThrowBoxNotFoundException() {
        // given
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.unregisterBox(1L))
                .isInstanceOf(BoxNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void givenValidInputsAndNewCurrency_whenAddMoney_thenInsertNewEntry() {
        // given
        Long boxId = 1L;
        String currencyCode = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);

        CollectionBox box = CollectionBox.builder()
                .id(boxId)
                .amounts(new ArrayList<>())
                .build();

        Currency currency = Currency.builder()
                .id(10L)
                .code(currencyCode)
                .build();

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.of(currency));

        // when
        boxService.addMoney(boxId, currencyCode, amount);

        // then
        assertThat(box.getAmounts()).hasSize(1);
        assertThat(box.getAmounts().getFirst().getAmount()).isEqualByComparingTo(amount);
        assertThat(box.getAmounts().getFirst().getCurrency()).isEqualTo(currency);
    }

    @Test
    void givenValidInputsAndExistingCurrency_whenAddMoney_thenIncreaseAmount() {
        // given
        Long boxId = 1L;
        String currencyCode = "EUR";
        BigDecimal initial = BigDecimal.valueOf(10);
        BigDecimal added = BigDecimal.valueOf(5);

        Currency currency = Currency.builder().id(100L).code(currencyCode).build();

        BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                .currency(currency)
                .amount(initial)
                .build();

        CollectionBox box = CollectionBox.builder()
                .id(boxId)
                .amounts(new ArrayList<>(List.of(entry)))
                .build();

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.of(currency));

        // when
        boxService.addMoney(boxId, currencyCode, added);

        // then
        assertThat(entry.getAmount()).isEqualByComparingTo("15");
    }

    @Test
    void givenNullAmount_whenAddMoney_thenThrowInvalidMoneyAmountException() {
        assertThatThrownBy(() -> boxService.addMoney(1L, "EUR", null))
                .isInstanceOf(InvalidMoneyAmountException.class);
    }

    @Test
    void givenNegativeAmount_whenAddMoney_thenThrowInvalidMoneyAmountException() {
        assertThatThrownBy(() -> boxService.addMoney(1L, "EUR", BigDecimal.valueOf(-5)))
                .isInstanceOf(InvalidMoneyAmountException.class);
    }

    @Test
    void givenNonExistingBox_whenAddMoney_thenThrowBoxNotFoundException() {
        // given
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.addMoney(1L, "EUR", BigDecimal.TEN))
                .isInstanceOf(BoxNotFoundException.class);
    }

    @Test
    void givenUnknownCurrency_whenAddMoney_thenThrowCurrencyNotFoundException() {
        // given
        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .amounts(new ArrayList<>())
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(currencyRepository.findByCode("ZZZ")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.addMoney(1L, "ZZZ", BigDecimal.TEN))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    void givenBoxWithSameCurrency_whenEmptyBox_thenTransferWithoutConversion() {
        // given
        String currencyCode = "EUR";
        BigDecimal initialAmount = BigDecimal.valueOf(100);
        BigDecimal existingBalance = BigDecimal.valueOf(50);

        Currency currency = Currency.builder().id(1L).code(currencyCode).build();
        FundraisingEvent event = FundraisingEvent.builder()
                .id(10L)
                .currency(currency)
                .accountBalance(existingBalance)
                .build();

        BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                .currency(currency)
                .amount(initialAmount)
                .build();

        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(event)
                .amounts(new ArrayList<>(List.of(entry)))
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when
        boxService.emptyBox(1L);

        // then
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getAccountBalance()).isEqualByComparingTo(existingBalance.add(initialAmount));
        verify(boxRepository).save(box);
        verify(eventRepository).save(event);
    }

    @Test
    void givenBoxWithMixedCurrencies_whenEmptyBox_thenConvertAndTransfer() {
        // given
        Currency eventCurrency = Currency.builder().id(1L).code("EUR").build();
        Currency usd = Currency.builder().id(2L).code("USD").build();

        FundraisingEvent event = FundraisingEvent.builder()
                .id(1L)
                .currency(eventCurrency)
                .accountBalance(BigDecimal.ZERO)
                .build();

        BoxCurrencyAmount eurAmount = BoxCurrencyAmount.builder()
                .currency(eventCurrency)
                .amount(BigDecimal.valueOf(50))
                .build();

        BoxCurrencyAmount usdAmount = BoxCurrencyAmount.builder()
                .currency(usd)
                .amount(BigDecimal.valueOf(10))
                .build();

        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(event)
                .amounts(new ArrayList<>(List.of(eurAmount, usdAmount)))
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(conversionClient.convert(BigDecimal.valueOf(10), "USD", "EUR"))
                .thenReturn(BigDecimal.valueOf(45));

        // when
        boxService.emptyBox(1L);

        // then
        assertThat(eurAmount.getAmount()).isZero();
        assertThat(usdAmount.getAmount()).isZero();
        assertThat(event.getAccountBalance()).isEqualByComparingTo("95");
    }

    @Test
    void givenBoxWithOnlyZeroAmounts_whenEmptyBox_thenNoTransferOccurs() {
        // given
        Currency currency = Currency.builder().id(1L).code("EUR").build();
        FundraisingEvent event = FundraisingEvent.builder()
                .id(1L)
                .currency(currency)
                .accountBalance(BigDecimal.valueOf(10))
                .build();

        BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                .currency(currency)
                .amount(BigDecimal.ZERO)
                .build();

        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(event)
                .amounts(List.of(entry))
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when
        boxService.emptyBox(1L);

        // then
        assertThat(event.getAccountBalance()).isEqualByComparingTo("10");
        assertThat(entry.getAmount()).isZero();
    }

    @Test
    void givenNonExistingBox_whenEmptyBox_thenThrowBoxNotFoundException() {
        // given
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> boxService.emptyBox(1L))
                .isInstanceOf(BoxNotFoundException.class);
    }

    @Test
    void givenBoxNotAssignedToEvent_whenEmptyBox_thenThrowBoxNotAssignedException() {
        // given
        CollectionBox box = CollectionBox.builder()
                .id(1L)
                .event(null)
                .amounts(List.of())
                .build();

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        // when / then
        assertThatThrownBy(() -> boxService.emptyBox(1L))
                .isInstanceOf(BoxNotAssignedException.class);
    }
}
