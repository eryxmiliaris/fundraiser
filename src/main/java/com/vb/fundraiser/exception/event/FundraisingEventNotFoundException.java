package com.vb.fundraiser.exception.event;

public class FundraisingEventNotFoundException extends RuntimeException {
    public FundraisingEventNotFoundException(Long id) {
        super("Fundraising event with ID " + id + " not found");
    }
}