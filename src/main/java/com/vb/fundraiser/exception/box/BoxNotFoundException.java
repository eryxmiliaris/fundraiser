package com.vb.fundraiser.exception.box;

public class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(Long id) {
        super("Collection box with ID " + id + " not found");
    }
}
