package hr.java.application.routeplanner.thread;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import hr.java.application.routeplanner.MainScreenController;
import hr.java.application.routeplanner.exceptions.FieldIsEmptyException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UpdateList implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    MapView mapView;
    ListView<String> results;
    Map<String, String> locations;
    volatile Boolean stopFlag;

    public UpdateList(MapView mapView, ListView<String> results, Map<String, String> locations) {
        this.mapView = mapView;
        this.results = results;
        this.locations = locations;
    }

    @Override
    public void run(){
        stopFlag = false;
        Node selelctedTextBox = mapView.getScene().getFocusOwner();
        if (selelctedTextBox instanceof TextField) {
            String text = ((TextField) selelctedTextBox).getText();
            try {
                if (Objects.equals(text, "")){
                    throw new FieldIsEmptyException("Cannot find location because field is empty.");
                }
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://graphhopper.com/api/1/geocode?q=" + ((TextField) selelctedTextBox).getText() + "&locale=en&key=503acf37-651a-4784-8d7d-73925f464e20")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();

                BufferedInputStream locationResponseBin = new BufferedInputStream(new URL(response.request().urlString()).openStream());

                if (stopFlag) return;

                extractLocations(new BufferedReader(new InputStreamReader(locationResponseBin, StandardCharsets.UTF_8)));

                ObservableList<String> names = FXCollections.observableList(locations.keySet().stream().toList());

                results.setTranslateY(0);
                results.setTranslateY(selelctedTextBox.localToScene(0,0).getY() - results.localToScene(0,0).getY() + 25);

                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        results.getSelectionModel().clearSelection();
                        results.setItems(names);
                    }
                });

                results.setVisible(true);
            } catch (IOException ex) {
                logger.error("Couldn't execute request", ex);
            } catch (FieldIsEmptyException ex){
                logger.error(ex.getMessage(), ex);
                results.setVisible(false);
            }
        }
    }

    private void extractLocations(BufferedReader locationResponse) throws IOException {
        JSONObject hits = new JSONObject(locationResponse.readLine());
        JSONArray a = (JSONArray) hits.get("hits");

        for (Object o : a)
        {
            JSONObject location = (JSONObject) o;
            Map<String, Object> locationMap = location.toMap();

            StringBuilder loc = new StringBuilder();

            String name = (String) locationMap.get("name");
            loc.append(String.format("%s ", name));
            if (locationMap.containsKey("housenumber")){
                String houseNumber = (String) locationMap.get("housenumber");
                loc.append(String.format("%s, ", houseNumber));
            }
            if (locationMap.containsKey("city")){
                String city = (String) locationMap.get("city");
                loc.append(String.format("%s, ", city));

            }
            String country = (String) locationMap.get("country");
            loc.append(String.format("%s", country));
            JSONObject point = (JSONObject) location.get("point");

            locations.put(loc.toString(), point.get("lat") + "," + point.get("lng"));

        }
    }

    public void setStopFlag(Boolean stopFlag) {
        this.stopFlag = stopFlag;
    }
}