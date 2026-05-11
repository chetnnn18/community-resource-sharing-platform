package com.sharenest.platform.exception;

public class AccessDeniedForResourceException extends RuntimeException {

    public AccessDeniedForResourceException(String message) {
        super(message);
    }
}
