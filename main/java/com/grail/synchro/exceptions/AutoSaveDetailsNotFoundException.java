package com.grail.synchro.exceptions;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class AutoSaveDetailsNotFoundException extends Exception {

    public AutoSaveDetailsNotFoundException() {
        super();
    }

    public AutoSaveDetailsNotFoundException(String message) {
        super(message);
    }

    public AutoSaveDetailsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoSaveDetailsNotFoundException(Throwable cause) {
        super(cause);
    }
}
