
package hr.java.application.routeplanner.entity;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import hr.java.application.routeplanner.MainScreenController;
import hr.java.application.routeplanner.mapLayer.DrawRide;
import hr.java.application.routeplanner.thread.CalculateData;
import hr.java.application.routeplanner.thread.WriteData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Ride extends NamedEntity {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private static MapLayer previousRide;
    private List<MapPoint> ridePoints = new ArrayList<>();
    private Integer refreshRateMillis = 200;
    private Integer timeout = 10;
    private Double[] distanceTraveled = {(double) 0};
    private Double[] speed = {(double) 0};
    private Double[] avgSpeed = {(double) 0};
    private Double[] maxSpeed = {(double) 0};
    private String username;
    Timeline timeline = null;

    public Ride() {}

    public Ride(String username) {
        this.username = username;
    }

    public Ride(String name, String ridePointsString, Double distanceTraveled, Double speed, Double avgSpeed, Double maxSpeed, String username) {
        super(name);
        String[] ridePointsArray = ridePointsString.split(";");
        for (String point: ridePointsArray) {
            ridePoints.add(new MapPoint(Double.parseDouble(point.split(",")[0]), Double.parseDouble(point.split(",")[1])));
        }
        this.distanceTraveled[0] = distanceTraveled;
        this.speed[0] = speed;
        this.avgSpeed[0] = avgSpeed;
        this.maxSpeed[0] = maxSpeed;
        this.username = username;
    }

    public Double getDistanceTraveled() {
        return distanceTraveled[0];
    }

    public Double getSpeed() {
        return speed[0];
    }

    public Double getAvgSpeed() {
        return avgSpeed[0];
    }

    public Double getMaxSpeed() {
        return maxSpeed[0];
    }

    public String getUsername() {
        return username;
    }

    public String getRidePointsString() {
        String ridePointsString = "";
        for (MapPoint point: ridePoints) {
            ridePointsString += ";" + point.getLatitude() + "," + point.getLongitude();
        }
        return ridePointsString.substring(1);
    }

    public List<MapPoint> getRidePoints() { return ridePoints; }

    public void startRide(MapView mapView, Label distanceTraveledLabel, Label currentSpeedLabel, Label averageSpeedLabel, Label maxSpeedLabel) {
        mapView.removeLayer(previousRide);
        final AtomicReference<Double>[] speedSum = new AtomicReference[]{new AtomicReference<>((double) 0)};
        distanceTraveled[0] = (double) 0;
        maxSpeed[0] = (double) 0;
        DrawRide routeTrack = new DrawRide(this);
        previousRide = routeTrack;
        final MapPoint[] pointPrev = {null};
        mapView.setZoom(16);
        mapView.removeLayer(routeTrack);
        routeTrack.route.getPoints().clear();
        mapView.addLayer(routeTrack);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("dat/movements.txt"))){
            lines = br.lines().toList();
        } catch (FileNotFoundException ex) {
            logger.error("The file does not exist", ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }

        final int[] i = {0};
        List<String> finalLines = lines;
        timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(refreshRateMillis),
                        event -> {
                            String line = finalLines.get(i[0]);
                            Double[] points = {Double.parseDouble(line.split(",")[0]), Double.parseDouble(line.split(",")[1])};
                            MapPoint point = new MapPoint(points[0], points[1]);
                            ridePoints.add(point);
                            Point2D mapPoint = new Point2D(point.getLatitude(), point.getLongitude());
                            routeTrack.route.getPoints().addAll(mapPoint.getX(), mapPoint.getY());
                            mapView.setCenter(mapPoint.getX(), mapPoint.getY());
                            if (i[0] != 0) {
                                ExecutorService executorService = Executors.newCachedThreadPool();
                                executorService.execute(new CalculateData(distanceTraveled, speed, avgSpeed, maxSpeed, point, pointPrev, speedSum, refreshRateMillis, i));
                                executorService.execute(new WriteData(distanceTraveled, speed, avgSpeed, maxSpeed, distanceTraveledLabel, currentSpeedLabel, averageSpeedLabel, maxSpeedLabel));
                                executorService.shutdown();
                                try {
                                    executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException ex) {
                                    logger.error(ex.getMessage(), ex);
                                }
                            }
                            pointPrev[0] = point;
                            i[0]++;
                        }
                )
        );
        timeline.setCycleCount(lines.size());
        timeline.play();
    }

    public void stopTimeline() { timeline.stop(); }
}
