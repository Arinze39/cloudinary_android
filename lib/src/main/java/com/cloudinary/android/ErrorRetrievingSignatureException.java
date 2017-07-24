package com.cloudinary.android;

// REVIEW should not be public
public class ErrorRetrievingSignatureException extends RuntimeException { // REVIEW should not be RuntimeException
    public ErrorRetrievingSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
