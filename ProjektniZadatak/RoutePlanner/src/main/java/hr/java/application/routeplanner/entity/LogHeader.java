package hr.java.application.routeplanner.entity;

import java.io.Serializable;

public class LogHeader implements Serializable {
    private String name;
    private Double version;

    public LogHeader() {
        this.name = "LogHeader";
        this.version = 1.0;
    }

    public String getName() {
        return name;
    }

    public Double getVersion() {
        return version;
    }
}
