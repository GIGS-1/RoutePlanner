package hr.java.application.routeplanner;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import hr.java.application.routeplanner.entity.*;
import hr.java.application.routeplanner.exceptions.FieldIsEmptyException;
import hr.java.application.routeplanner.exceptions.NoRouteException;
import hr.java.application.routeplanner.mapLayer.DrawRoute;
import hr.java.application.routeplanner.records.User;
import hr.java.application.routeplanner.thread.UpdateList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RideScreenController implements Initializable{
    private static final Logger logger = LoggerFactory.getLogger(RideScreenController.class);

    private static Ride ride;
    public User user;

    @FXML
    private ImageView logo;
    @FXML
    private ImageView deleteButton;
    @FXML
    private ImageView backButton;
    @FXML
    MapView mapView = new MapView();
    @FXML
    Label distanceTraveled;
    @FXML
    Label currentSpeed;
    @FXML
    Label averageSpeed;
    @FXML
    Label maxSpeed;

    static Label distanceTraveledStatic;
    static Label currentSpeedStatic;
    static Label averageSpeedStatic;
    static Label maxSpeedStatic;

    static MapView staticMapView;
    static User staticUser;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logo.setImage(new Image(new FileInputStream("img/logo.png")));
            deleteButton.setImage(new Image(new FileInputStream("img/delete.png")));
            backButton.setImage(new Image(new FileInputStream("img/back.png")));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        user = LoginScreenController.user;

        mapInitializer();

        distanceTraveledStatic = distanceTraveled;
        currentSpeedStatic = currentSpeed;
        maxSpeedStatic = maxSpeed;
        averageSpeedStatic = averageSpeed;

        staticMapView = mapView;
        staticUser = user;
    }

    public void mapInitializer() {
        mapView.setZoom(1);
        mapView.setCenter(0, 0);
    }

    @FXML
    private void onBackButtonPress() {
        FXMLLoader fxmlLoader = null;
        if (Objects.equals(user.role(), "Admin")){
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminMenuScreen.fxml"));
        }
        else {
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("userMenuScreen.fxml"));
        }
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    @FXML
    private void onDeleteButonPress() {
        try {
            Database.removeRide(ride);
        } catch (SQLException | IOException ex) {
            logger.error("Error communicating with database", ex);
        }
        try {
            FXMLLoader fxmlLoader;
            if (Objects.equals(user.role(), "Admin")) {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminMenuScreen.fxml"));
            } else {
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("userMenuScreen.fxml"));
            }
            Scene scene = null;
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    public static void drawRide(String rideName) {
        try {
            List<Ride> rides = Database.getRidesDB();
            for (Ride r: rides) {
                if (r.getName().equals(rideName)){
                    ride = r;
                    List<Waypoint> waypoints = new ArrayList<>();
                    for (MapPoint mp: r.getRidePoints()) {
                        waypoints.add(new Waypoint<>(mp.getLatitude(), mp.getLongitude()));
                    }
                    Route route = new Route.RouteBuilder().name("Temp").routePoints(waypoints).build();
                    staticMapView.addLayer(new DrawRoute(route));
                    staticMapView.setZoom(15);
                    staticMapView.setCenter(waypoints.getFirst().getLat(), waypoints.getFirst().getLng());
                    if (MainScreenController.unitType.equals("Metric")){
                        distanceTraveledStatic.setText(r.getDistanceTraveled().toString());
                        currentSpeedStatic.setText(r.getSpeed().toString());
                        averageSpeedStatic.setText(r.getAvgSpeed().toString());
                        maxSpeedStatic.setText(r.getMaxSpeed().toString());
                    }
                    else {
                        distanceTraveledStatic.setText(String.format("%.2f", (r.getDistanceTraveled()/1.609)));
                        currentSpeedStatic.setText(String.format("%.2f", (r.getSpeed()/1.609)));
                        averageSpeedStatic.setText(String.format("%.2f", (r.getAvgSpeed()/1.609)));
                        maxSpeedStatic.setText(String.format("%.2f", (r.getMaxSpeed()/1.609)));
                    }

                    break;
                }
            }
        } catch (SQLException | IOException ex) {
            logger.error("Error communicating with database", ex);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}