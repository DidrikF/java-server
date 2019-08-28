package com.didrikfleischer.app.core.request;


public class HttpFormatException extends Exception {

    private static final long serialVersionUID = 1L; // dont know why this is needed...

    public HttpFormatException(String message) {
        super(message);
    }
}