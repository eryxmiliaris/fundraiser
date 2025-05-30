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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private static final Long BOX_ID = 1L;
    private static final Long EVENT_ID = 10L;
    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    private Currency eurCurrency;
    private Currency usdCurrency;
    private FundraisingEvent event;

    @BeforeEach
    void setUp() {
        eurCurrency = Currency.builder().id(1L).code(EUR).build();
        usdCurrency = Currency.builder().id(2L).code(USD).build();

        event = FundraisingEvent.builder()
                .id(EVENT_ID)
                .name("Test Event")
                .currency(eurCurrency)
                .accountBalance(BigDecimal.ZERO)
                .build();
    }

    private CollectionBox emptyBox(Long id, boolean assigned) {
        return CollectionBox.builder()
                .id(id)
                .event(assigned ? event : null)
                .amounts(new ArrayList<>())
                .build();
    }

    private CollectionBox nonEmptyBox(Long id, boolean assigned) {
        BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                .currency(eurCurrency)
                .amount(BigDecimal.TEN)
                .build();
        return CollectionBox.builder()
                .id(id)
                .amounts(List.of(entry))
                .event(assigned ? event : null)
                .build();
    }

    @Nested
    class RegisterBox {
        @Test
        void givenNoArguments_whenRegisterNewBox_thenReturnDtoWithIdAndDefaults() {
            // given
            CollectionBox savedBox = emptyBox(BOX_ID, false);
            when(boxRepository.save(any())).thenReturn(savedBox);

            // when
            CollectionBoxDTO result = boxService.registerNewBox();

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(BOX_ID);
            assertThat(result.assigned()).isFalse();
            assertThat(result.empty()).isTrue();
        }
    }

    @Nested
    class AssignBoxToEvent {
        @Test
        void givenUnassignedEmptyBoxAndValidEvent_whenAssignBoxToEvent_thenReturnAssignedDto() {
            // given
            CollectionBox box = emptyBox(BOX_ID, false);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(boxRepository.save(any())).thenReturn(box);

            // when
            CollectionBoxDTO result = boxService.assignBoxToEvent(BOX_ID, EVENT_ID);

            // then
            assertThat(result.id()).isEqualTo(BOX_ID);
            assertThat(result.assigned()).isTrue();
            assertThat(result.empty()).isFalse();
        }

        @Test
        void givenNonExistingBox_whenAssignBoxToEvent_thenThrowBoxNotFoundException() {
            // given
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .isInstanceOf(BoxNotFoundException.class);
        }

        @Test
        void givenBoxAlreadyAssigned_whenAssignBoxToEvent_thenThrowBoxAlreadyAssignedException() {
            // given
            CollectionBox box = emptyBox(BOX_ID, true);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.assignBoxToEvent(BOX_ID, 2L))
                    .isInstanceOf(BoxAlreadyAssignedException.class);
        }

        @Test
        void givenBoxNotEmpty_whenAssignBoxToEvent_thenThrowNotEmptyBoxAssignmentException() {
            // given
            CollectionBox box = nonEmptyBox(BOX_ID, false);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .isInstanceOf(NotEmptyBoxAssignmentException.class);
        }

        @Test
        void givenEventDoesNotExist_whenAssignBoxToEvent_thenThrowFundraisingEventNotFoundException() {
            // given
            CollectionBox box = emptyBox(BOX_ID, false);
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.assignBoxToEvent(BOX_ID, EVENT_ID))
                    .isInstanceOf(FundraisingEventNotFoundException.class);
        }
    }

    @Nested
    class GetBoxes {
        @Test
        void givenNoBoxesExist_whenGetAllBoxes_thenReturnEmptyPage() {
            // given
            when(boxRepository.findByIsDeletedFalse(any(Pageable.class))).thenReturn(Page.empty());

            // when
            Page<CollectionBoxDTO> result = boxService.getAllBoxes(0, 10, "asc");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void givenBoxesWithDifferentStates_whenGetAllBoxes_thenReturnCorrectDTOs() {
            // given
            CollectionBox emptyBox = emptyBox(1L, false);

            CollectionBox assignedEmptyBox = emptyBox(2L, true);

            CollectionBox nonEmptyUnassignedBox = nonEmptyBox(3L, false);

            Page<CollectionBox> page = new PageImpl<>(List.of(emptyBox, assignedEmptyBox, nonEmptyUnassignedBox));
            when(boxRepository.findByIsDeletedFalse(any(Pageable.class))).thenReturn(page);

            // when
            Page<CollectionBoxDTO> result = boxService.getAllBoxes(0, 10, "asc");

            // then
            assertThat(result).hasSize(3);
            assertThat(result).anyMatch(dto -> dto.id().equals(1L) && !dto.assigned() && dto.empty());
            assertThat(result).anyMatch(dto -> dto.id().equals(2L) && dto.assigned() && dto.empty());
            assertThat(result).anyMatch(dto -> dto.id().equals(3L) && !dto.assigned() && !dto.empty());
        }

        @Test
        void givenInvalidPaginationInput_whenGetAllBoxes_thenThrowIllegalArgument() {
            // when / then
            assertThatThrownBy(() -> boxService.getAllBoxes(-1, 10, "asc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Page index must not be negative");

            assertThatThrownBy(() -> boxService.getAllBoxes(0, 0, "asc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Page size must be between 1 and 100");

            assertThatThrownBy(() -> boxService.getAllBoxes(0, 10, "invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sort direction must be 'asc' or 'desc'");
        }
    }

    @Nested
    class UnregisterBox {
        @Test
        void givenExistingBox_whenUnregisterBox_thenMarkAsDeleted() {
            // given
            CollectionBox box = emptyBox(BOX_ID, false);
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when
            boxService.unregisterBox(BOX_ID);

            // then
            assertThat(box.isDeleted()).isTrue();
            verify(boxRepository).save(box);
        }

        @Test
        void givenMissingBox_whenUnregisterBox_thenThrowBoxNotFoundException() {
            // given
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.unregisterBox(BOX_ID))
                    .isInstanceOf(BoxNotFoundException.class);
        }
    }

    @Nested
    class AddMoney {
        @Test
        void givenValidInputsAndNewCurrency_whenAddMoney_thenInsertNewEntry() {
            // given
            BigDecimal amount = BigDecimal.valueOf(100);

            CollectionBox box = emptyBox(BOX_ID, true);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(currencyRepository.findByCode(EUR)).thenReturn(Optional.of(eurCurrency));

            // when
            boxService.addMoney(BOX_ID, EUR, amount);

            // then
            assertThat(box.getAmounts()).hasSize(1);
            assertThat(box.getAmounts().getFirst().getAmount()).isEqualByComparingTo(amount);
            assertThat(box.getAmounts().getFirst().getCurrency()).isEqualTo(eurCurrency);
        }

        @Test
        void givenValidInputsAndExistingCurrency_whenAddMoney_thenIncreaseAmount() {
            // given
            BigDecimal initial = BigDecimal.valueOf(10);
            BigDecimal added = BigDecimal.valueOf(5);

            BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                    .currency(eurCurrency)
                    .amount(initial)
                    .build();

            CollectionBox box = CollectionBox.builder()
                    .id(BOX_ID)
                    .event(event)
                    .amounts(new ArrayList<>(List.of(entry)))
                    .build();

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(currencyRepository.findByCode(EUR)).thenReturn(Optional.of(eurCurrency));

            // when
            boxService.addMoney(BOX_ID, EUR, added);

            // then
            assertThat(entry.getAmount()).isEqualByComparingTo("15");
        }

        @Test
        void givenBoxNotAssignedToEvent_whenAddMoney_thenThrowBoxNotAssignedException() {
            // given
            CollectionBox box = emptyBox(BOX_ID, false);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.addMoney(BOX_ID, EUR, BigDecimal.TEN))
                    .isInstanceOf(BoxNotAssignedException.class);
        }

        @Test
        void givenNullAmount_whenAddMoney_thenThrowInvalidMoneyAmountException() {
            assertThatThrownBy(() -> boxService.addMoney(BOX_ID, EUR, null))
                    .isInstanceOf(InvalidMoneyAmountException.class);
        }

        @Test
        void givenNegativeAmount_whenAddMoney_thenThrowInvalidMoneyAmountException() {
            assertThatThrownBy(() -> boxService.addMoney(BOX_ID, EUR, BigDecimal.valueOf(-5)))
                    .isInstanceOf(InvalidMoneyAmountException.class);
        }

        @Test
        void givenNonExistingBox_whenAddMoney_thenThrowBoxNotFoundException() {
            // given
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.addMoney(BOX_ID, EUR, BigDecimal.TEN))
                    .isInstanceOf(BoxNotFoundException.class);
        }

        @Test
        void givenInvalidCurrency_whenAddMoney_thenThrowCurrencyNotFoundException() {
            // given
            String invalidCurrencyCode = "ZZZ";
            CollectionBox box = emptyBox(BOX_ID, true);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(currencyRepository.findByCode(invalidCurrencyCode)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.addMoney(BOX_ID, invalidCurrencyCode, BigDecimal.TEN))
                    .isInstanceOf(CurrencyNotFoundException.class);
        }
    }


    @Nested
    class EmptyBox {
        @Test
        void givenBoxWithSameCurrency_whenEmptyBox_thenTransferWithoutConversion() {
            // given
            BigDecimal initialAmount = BigDecimal.valueOf(100);
            BigDecimal existingBalance = BigDecimal.valueOf(50);

            FundraisingEvent event = FundraisingEvent.builder()
                    .id(EVENT_ID)
                    .currency(eurCurrency)
                    .accountBalance(existingBalance)
                    .build();

            BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                    .currency(eurCurrency)
                    .amount(initialAmount)
                    .build();

            CollectionBox box = CollectionBox.builder()
                    .id(BOX_ID)
                    .event(event)
                    .amounts(new ArrayList<>(List.of(entry)))
                    .build();

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when
            boxService.emptyBox(BOX_ID);

            // then
            assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(event.getAccountBalance()).isEqualByComparingTo(existingBalance.add(initialAmount));
            verify(boxRepository).save(box);
            verify(eventRepository).save(event);
        }

        @Test
        void givenBoxWithMixedCurrencies_whenEmptyBox_thenConvertAndTransfer() {
            // given
            FundraisingEvent event = FundraisingEvent.builder()
                    .id(EVENT_ID)
                    .currency(eurCurrency)
                    .accountBalance(BigDecimal.ZERO)
                    .build();

            BoxCurrencyAmount eurAmount = BoxCurrencyAmount.builder()
                    .currency(eurCurrency)
                    .amount(BigDecimal.valueOf(50))
                    .build();

            BoxCurrencyAmount usdAmount = BoxCurrencyAmount.builder()
                    .currency(usdCurrency)
                    .amount(BigDecimal.valueOf(10))
                    .build();

            CollectionBox box = CollectionBox.builder()
                    .id(BOX_ID)
                    .event(event)
                    .amounts(new ArrayList<>(List.of(eurAmount, usdAmount)))
                    .build();

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));
            when(conversionClient.convert(BigDecimal.valueOf(10), "USD", "EUR"))
                    .thenReturn(BigDecimal.valueOf(45));

            // when
            boxService.emptyBox(BOX_ID);

            // then
            assertThat(eurAmount.getAmount()).isZero();
            assertThat(usdAmount.getAmount()).isZero();
            assertThat(event.getAccountBalance()).isEqualByComparingTo("95");
        }

        @Test
        void givenBoxWithOnlyZeroAmounts_whenEmptyBox_thenThrowEmptyBoxMoneyTransferException() {
            // given
            BoxCurrencyAmount entry = BoxCurrencyAmount.builder()
                    .currency(eurCurrency)
                    .amount(BigDecimal.ZERO)
                    .build();

            CollectionBox box = CollectionBox.builder()
                    .id(BOX_ID)
                    .event(event)
                    .amounts(List.of(entry))
                    .build();

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.emptyBox(BOX_ID))
                    .isInstanceOf(EmptyBoxMoneyTransferException.class);
        }

        @Test
        void givenBoxWithNoAmounts_whenEmptyBox_thenThrowEmptyBoxMoneyTransferException() {
            // given
            CollectionBox box = emptyBox(BOX_ID, true);

            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.emptyBox(BOX_ID))
                    .isInstanceOf(EmptyBoxMoneyTransferException.class);
        }

        @Test
        void givenNonExistingBox_whenEmptyBox_thenThrowBoxNotFoundException() {
            // given
            when(boxRepository.findByIdAndIsDeletedFalse(BOX_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> boxService.emptyBox(BOX_ID))
                    .isInstanceOf(BoxNotFoundException.class);
        }

        @Test
        void givenBoxNotAssignedToEvent_whenEmptyBox_thenThrowBoxNotAssignedException() {
            // given
            CollectionBox box = emptyBox(BOX_ID, false);

            when(boxRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(box));

            // when / then
            assertThatThrownBy(() -> boxService.emptyBox(BOX_ID))
                    .isInstanceOf(BoxNotAssignedException.class);
        }
    }
}
