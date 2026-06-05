package com.catedral.catedraletl.exception;

public class LpgParseException extends RuntimeException {

    public LpgParseException(String message) {
        super(message);
    }

    public LpgParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
