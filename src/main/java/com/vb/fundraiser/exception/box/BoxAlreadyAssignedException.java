package com.vb.fundraiser.exception.box;

public class BoxAlreadyAssignedException extends RuntimeException {
    public BoxAlreadyAssignedException(Long boxId) {
        super("Box " + boxId + " is already assigned to a fundraising event");
    }
}