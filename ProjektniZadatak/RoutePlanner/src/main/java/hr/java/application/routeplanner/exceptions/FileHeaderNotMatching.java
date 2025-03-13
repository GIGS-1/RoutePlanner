package hr.java.application.routeplanner.exceptions;

public class FileHeaderNotMatching extends RuntimeException{
    public FileHeaderNotMatching(String message){
        super(message);
    }
    public FileHeaderNotMatching(Throwable cause){
        super(cause);
    }
    public FileHeaderNotMatching(String message, Throwable cause){
        super(message, cause);
    }
}
