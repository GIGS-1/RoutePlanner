package hr.java.application.routeplanner.exceptions;

public class NoRouteException extends RuntimeException{
    public NoRouteException(String message){
        super(message);
    }
    public NoRouteException(Throwable cause){
        super(cause);
    }
    public NoRouteException(String message, Throwable cause){
        super(message, cause);
    }
}