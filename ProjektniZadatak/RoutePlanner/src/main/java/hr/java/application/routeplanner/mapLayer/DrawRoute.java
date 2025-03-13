package hr.java.application.routeplanner.mapLayer;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.entity.Route;
import hr.java.application.routeplanner.entity.Waypoint;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

public class DrawRoute extends MapLayer {
    private final Polyline routeLine;
    private final Route route;

    public DrawRoute(Route route) {
        this.route = route;
        routeLine = new Polyline();
        routeLine.setStroke(Color.BLUE);
        routeLine.setStrokeWidth(3);
        this.getChildren().add(routeLine);
    }

    @Override
    protected void layoutLayer() {
        routeLine.getPoints().clear();
        for (Waypoint candidate : route.getRoutePoints()) {
            Point2D mapPoint = getMapPoint(candidate.getLat(), candidate.getLng());
            routeLine.getPoints().addAll(mapPoint.getX(), mapPoint.getY());
        }
    }
}