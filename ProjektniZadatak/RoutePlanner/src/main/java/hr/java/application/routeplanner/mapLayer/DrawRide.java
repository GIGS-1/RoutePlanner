package hr.java.application.routeplanner.mapLayer;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.entity.Ride;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

public class DrawRide extends MapLayer {
    public final Polyline route;
    public Ride ride;

    public DrawRide(Ride ride) {
        this.ride = ride;
        route = new Polyline();
        route.setStroke(Color.RED);
        route.setStrokeWidth(3);
        this.getChildren().add(route);
    }

    @Override
    protected void layoutLayer() {
        route.getPoints().clear();
        for (MapPoint candidate : ride.getRidePoints()) {
            Point2D mapPoint = getMapPoint(candidate.getLatitude(), candidate.getLongitude());
            route.getPoints().addAll(mapPoint.getX(), mapPoint.getY());
        }
    }
}