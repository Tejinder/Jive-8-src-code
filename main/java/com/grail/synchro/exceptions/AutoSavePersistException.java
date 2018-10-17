package com.grail.synchro.exceptions;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class AutoSavePersistException extends Exception {
    public AutoSavePersistException() {

    }

    public AutoSavePersistException(String message) {
        super(message);
    }

    public AutoSavePersistException(String message, Throwable cause) {
        super(message, cause);
    }

    public AutoSavePersistException(Throwable cause) {
        super(cause);
    }
}
