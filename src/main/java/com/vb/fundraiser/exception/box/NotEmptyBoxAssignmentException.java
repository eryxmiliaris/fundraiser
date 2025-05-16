package com.vb.fundraiser.exception.box;

public class NotEmptyBoxAssignmentException extends RuntimeException {
    public NotEmptyBoxAssignmentException(Long boxId) {
        super("Cannot assign box " + boxId + " because it is not empty");
    }
}