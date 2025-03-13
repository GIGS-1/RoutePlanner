package hr.java.application.routeplanner.entity;

import java.io.Serializable;

public class Waypoint<T1, T2> extends NamedEntity implements Serializable {
    private T2 lat, lng;

    public Waypoint(T1 name, T2 lat, T2 lng) {
        super(name);
        this.lat = lat;
        this.lng = lng;
    }

    public Waypoint(T2 lat, T2 lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() { return (Double) lat; }

    public void setLat(T2 lat) { this.lat = lat; }

    public Double getLng() { return (Double) lng; }

    public void setLng(T2 lng) { this.lng = lng; }
}
