package com.didrikfleischer.app.core.router;

public class FilterException extends Exception {
    static final long serialVersionUID = 123;
    public String responseBody;
    public int statusCode;

    public FilterException(String message, int statusCode) {
        super(message);
        this.responseBody = message;
        this.statusCode = statusCode;
    }

    /**
     * @return the responseBody
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
}