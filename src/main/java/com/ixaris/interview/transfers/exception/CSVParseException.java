package com.ixaris.interview.transfers.exception;

/**
 * Represents an exception class to handle csv parse operation
 */
public class CSVParseException extends RuntimeException{
    public CSVParseException(String message) {
        super(message);
    }
}
