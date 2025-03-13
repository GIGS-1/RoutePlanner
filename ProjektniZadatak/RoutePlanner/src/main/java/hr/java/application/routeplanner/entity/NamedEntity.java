package hr.java.application.routeplanner.entity;

import java.io.Serializable;

public abstract class NamedEntity<T> implements Serializable {
    private T name;

    public NamedEntity() {}

    public NamedEntity(T name) {
        this.name = name;
    }

    public T getName() {
        return name;
    }

    public void setName(T name) {
        this.name = name;
    }
}
