package hr.java.application.routeplanner.exceptions;

public class FieldIsEmptyException extends RuntimeException{
    public FieldIsEmptyException(String message){
        super(message);
    }
    public FieldIsEmptyException(Throwable cause){
        super(cause);
    }
    public FieldIsEmptyException(String message, Throwable cause){
        super(message, cause);
    }
}
