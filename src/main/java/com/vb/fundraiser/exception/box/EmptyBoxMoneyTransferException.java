package com.vb.fundraiser.exception.box;

public class EmptyBoxMoneyTransferException extends RuntimeException{
    public EmptyBoxMoneyTransferException(Long id) {
        super("Attempt to transfer money from box " + id + " which is empty");
    }
}
