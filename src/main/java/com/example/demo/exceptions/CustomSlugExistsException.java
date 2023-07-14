package com.example.demo.exceptions;

public class CustomSlugExistsException extends RuntimeException {
    public CustomSlugExistsException(String message) {
        super(message);
    }
}
