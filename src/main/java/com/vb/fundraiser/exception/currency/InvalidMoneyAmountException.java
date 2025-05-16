package com.vb.fundraiser.exception.currency;

import java.math.BigDecimal;

public class InvalidMoneyAmountException extends RuntimeException {
    public InvalidMoneyAmountException(BigDecimal amount) {
        super("Invalid amount: " + amount + ". Amount must be greater than 0");
    }
}
