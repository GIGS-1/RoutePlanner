package hr.java.application.routeplanner.entity;

import java.io.Serial;
import java.io.Serializable;

public class Log implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private String date;
    private String title;
    private String message;
    private String user;
    private String role;

    public Log() {}

    public Log(String date, String title, String message, String user, String role) {
        this.date = date;
        this.title = title;
        this.message = message;
        this.user = user;
        this.role = role;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }
}
