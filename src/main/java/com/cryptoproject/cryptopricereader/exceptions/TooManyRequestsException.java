package com.cryptoproject.cryptopricereader.exceptions;

public class TooManyRequestsException extends RuntimeException{
    public TooManyRequestsException(String message) {
        super(message);
    }
}
