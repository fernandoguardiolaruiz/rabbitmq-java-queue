package com.middleware.jms.converter;

public class ConverterException extends Exception {

    public ConverterException(Exception cause) {
        super(cause);
    }

    public ConverterException(String message) {
        super(message);
    }
}
