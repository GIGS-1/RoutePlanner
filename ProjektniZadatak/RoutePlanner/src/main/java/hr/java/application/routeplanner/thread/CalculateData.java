package hr.java.application.routeplanner.thread;

import com.gluonhq.maps.MapPoint;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CalculateData extends DataThread implements Runnable{
    static Integer refreshRateMillis;
    Double[] distanceTraveled;
    Double[] speed;
    Double[] avgSpeed;
    Double[] maxSpeed;
    MapPoint point;
    MapPoint[] pointPrev;
    AtomicReference<Double>[] speedSum;
    int[] i;

    public CalculateData(Double[] distanceTraveled, Double[] speed, Double[] avgSpeed, Double[] maxSpeed, MapPoint point, MapPoint[] pointPrev, AtomicReference<Double>[] speedSum, Integer refreshRateMillis, int[] i) {
        this.distanceTraveled = distanceTraveled;
        this.speed = speed;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;
        this.point = point;
        this.pointPrev = pointPrev;
        this.speedSum= speedSum;
        this.refreshRateMillis = refreshRateMillis;
        this.i = i;
    }

    @Override
    public void run(){
        super.calculateData(distanceTraveled, speed, avgSpeed, maxSpeed, point, pointPrev, speedSum, refreshRateMillis, i);
    }
}
