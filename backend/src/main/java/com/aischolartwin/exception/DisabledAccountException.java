package com.aischolartwin.exception;

public class DisabledAccountException extends RuntimeException {
    public DisabledAccountException(String message) {
        super(message);
    }
}

