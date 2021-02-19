package org.apache.nifi.security.util.crypto;

/**
 * Exception indicating an error occurred instantiating a SecureHasher.
 */
@SuppressWarnings("serial")
public class SecureHasherException extends RuntimeException {

    public SecureHasherException(String message) {
        super(message);
    }

    public SecureHasherException(String message, Throwable cause) {
        super(message, cause);
    }
}