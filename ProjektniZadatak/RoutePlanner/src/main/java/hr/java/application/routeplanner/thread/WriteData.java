package hr.java.application.routeplanner.thread;

import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.MainScreenController;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.concurrent.atomic.AtomicReference;

public class WriteData extends DataThread implements Runnable{
    Double[] distanceTraveled;
    Double[] speed;
    Double[] avgSpeed;
    Double[] maxSpeed;
    Label distanceTraveledLabel;
    Label currentSpeedLabel;
    Label averageSpeedLabel;
    Label maxSpeedLabel;

    public WriteData(Double[] distanceTraveled, Double[] speed, Double[] avgSpeed, Double[] maxSpeed, Label distanceTraveledLabel, Label currentSpeedLabel, Label averageSpeedLabel, Label maxSpeedLabel) {
        this.distanceTraveled = distanceTraveled;
        this.speed = speed;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;
        this.distanceTraveledLabel = distanceTraveledLabel;
        this.currentSpeedLabel = currentSpeedLabel;
        this.averageSpeedLabel = averageSpeedLabel;
        this.maxSpeedLabel = maxSpeedLabel;
    }

    @Override
    public void run(){
        super.writeData(distanceTraveled, speed, avgSpeed, maxSpeed, distanceTraveledLabel, currentSpeedLabel, averageSpeedLabel, maxSpeedLabel);
    }
}