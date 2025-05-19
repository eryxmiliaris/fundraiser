package com.vb.fundraiser.exception.event;

public class FundraisingEventAlreadyExistsException extends RuntimeException {
    public FundraisingEventAlreadyExistsException(String name) {
        super("Fundraising event with name '" + name + "' already exists");
    }
}