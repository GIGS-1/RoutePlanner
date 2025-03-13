package hr.java.application.routeplanner.exceptions;

public class PasswordTooShortException extends Exception {
    public PasswordTooShortException(String message){
        super(message);
    }
    public PasswordTooShortException(Throwable cause){
        super(cause);
    }
    public PasswordTooShortException(String message, Throwable cause){
        super(message, cause);
    }
}