package com.vb.fundraiser.exception.box;

public class BoxNotAssignedException extends RuntimeException {
    public BoxNotAssignedException(Long boxId) {
        super("Box " + boxId + " is not assigned to any fundraising event");
    }
}