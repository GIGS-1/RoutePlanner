package hr.java.application.routeplanner.thread;

import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.MainScreenController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DataThread {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    public static volatile Boolean operationInProgress = false;
    boolean turn = false;

    public synchronized void writeData(Double[] distanceTraveled, Double[] speed, Double[] avgSpeed, Double[] maxSpeed, Label distanceTraveledLabel, Label currentSpeedLabel, Label averageSpeedLabel, Label maxSpeedLabel) {

        while(operationInProgress) {
            try {
                wait();
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        operationInProgress = true;

        Platform.runLater(() -> {
            if (MainScreenController.unitType.equals("Metric")){
                distanceTraveledLabel.setText(String.format("%.2f", distanceTraveled[0]));
                currentSpeedLabel.setText(String.format("%.2f", speed[0]));
                averageSpeedLabel.setText(String.format("%.2f", avgSpeed[0]));
                maxSpeedLabel.setText(String.format("%.2f", maxSpeed[0]));
            }
            else {
                distanceTraveledLabel.setText(String.format("%.2f", distanceTraveled[0]/1.609));
                currentSpeedLabel.setText(String.format("%.2f", speed[0]/1.609));
                averageSpeedLabel.setText(String.format("%.2f", avgSpeed[0]/1.609));
                maxSpeedLabel.setText(String.format("%.2f", maxSpeed[0]/1.609));
            }
        });

        operationInProgress = false;

        notifyAll();
    }

    public synchronized void calculateData(Double[] distanceTraveled, Double[] speed, Double[] avgSpeed, Double[] maxSpeed, MapPoint point, MapPoint[] pointPrev, AtomicReference<Double>[] speedSum, Integer refreshRateMillis, int[] i) {

        while(operationInProgress) {
            try {
                wait();
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        operationInProgress = true;

        Double distance = calculateDistance(point, pointPrev[0]);
        distanceTraveled[0] += distance/1000;
        speed[0] = calculateSpeed(distance, refreshRateMillis);
        speedSum[0].updateAndGet(v -> v + speed[0]);
        avgSpeed[0] = speedSum[0].get()/i[0];
        if (speed[0] > maxSpeed[0]){
            maxSpeed[0] = speed[0];
        }

        operationInProgress = false;

        notifyAll();
    }

    private static Double calculateDistance(MapPoint point, MapPoint pointPrev) {
        Double lat1 = pointPrev.getLatitude();
        Double lat2 = point.getLatitude();
        Double lon1 = pointPrev.getLongitude();
        Double lon2 = point.getLongitude();

        Double earthRadius = 6371e3;
        Double latRad1 = lat1 * Math.PI/180;
        Double latRad2 = lat2 * Math.PI/180;
        Double deltaLat = (lat2-lat1) * Math.PI/180;
        Double deltaLng = (lon2-lon1) * Math.PI/180;

        Double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) + Math.cos(latRad1) * Math.cos(latRad2) * Math.sin(deltaLng/2) * Math.sin(deltaLng/2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        Double d = earthRadius * c;
        return d;
    }

    private static Double calculateSpeed(Double distance, Integer refreshRateMillis) {
        return (distance/1000)/((double)refreshRateMillis/1000/60/60);
    }
}
