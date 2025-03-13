package hr.java.application.routeplanner;

import com.github.vincentrussell.ini.Ini;
import com.gluonhq.maps.*;
import com.sun.jna.Native;
import hr.java.application.routeplanner.entity.*;
import hr.java.application.routeplanner.entity.Dictionary;
import hr.java.application.routeplanner.exceptions.FieldIsEmptyException;
import hr.java.application.routeplanner.exceptions.NoRouteException;
import hr.java.application.routeplanner.interfaces.AlertLib;
import hr.java.application.routeplanner.interfaces.WinRegLib;
import hr.java.application.routeplanner.records.User;
import hr.java.application.routeplanner.mapLayer.DrawRoute;
import hr.java.application.routeplanner.thread.CalculateData;
import hr.java.application.routeplanner.thread.UpdateList;
import hr.java.application.routeplanner.thread.WriteData;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
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
import java.util.concurrent.TimeUnit;

import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;


public class MainScreenController implements Initializable{
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    public static String unitType = "Metric";
    List<TextField> stops = new ArrayList<>();
    List<Button> removeStopButtons = new ArrayList<>();
    List<Button> chooseOnMapButtons = new ArrayList<>();
    Map<String, String> locations = new TreeMap<>();
    Map<String, String> finalLocations = new TreeMap<>();
    List<Runnable> runningUpdateThreads = new ArrayList<>();
    Route route = null;
    Ride ride;
    Boolean check = true;
    public User user;

    @FXML
    private VBox vb;
    @FXML
    private TextField beginning;
    @FXML
    private TextField end;
    @FXML
    private Button beginningButton;
    @FXML
    private Button endButton;
    @FXML
    private ImageView logo;
    @FXML
    private ImageView settingsButton;
    @FXML
    private ImageView saveRouteButton;
    @FXML
    MapView mapView = new MapView();
    @FXML
    ListView<String> results = new ListView<>();
    @FXML
    Label distanceTraveled;
    @FXML
    Label currentSpeed;
    @FXML
    Label averageSpeed;
    @FXML
    Label maxSpeed;
    @FXML
    private Button startButton;

    @FXML
    private Button search;
    @FXML
    private Button addStop;
    @FXML
    private Text distanceLabel;
    @FXML
    private Text speedLabel;
    @FXML
    private Text avgSpeedLabel;
    @FXML
    private Text maxSpeedLabel;
    @FXML
    private Label saveRouteLabel;
    @FXML
    private Button savePopup;
    @FXML
    private Button cancelPopup;

    @FXML
    private VBox popup;
    @FXML
    private TextField saveRouteName;

    static MapView staticMapView;
    static User staticUser;

    private MapLayer routeLayer;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logo.setImage(new Image(new FileInputStream("img/logo.png")));
            settingsButton.setImage(new Image(new FileInputStream("img/menu.png")));
            saveRouteButton.setImage(new Image(new FileInputStream("img/save.png")));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        user = LoginScreenController.user;

        results.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null){
                Node selelctedTextBox = mapView.getScene().getFocusOwner();
                if (selelctedTextBox instanceof TextField) {
                    ((TextField) selelctedTextBox).setText(newVal);
                    finalLocations.put(newVal, locations.get(newVal));
                    mapView.setZoom(15);
                    mapView.setCenter(Double.parseDouble(locations.get(newVal).split(",")[0]), Double.parseDouble(locations.get(newVal).split(",")[1]));
                    results.setVisible(false);
                }
            }
        });

        beginningButton.setOnAction(event -> onChooseOnMapButtonPress(beginning));
        endButton.setOnAction(event -> onChooseOnMapButtonPress(end));

        mapInitializer();

        popup.setVisible(false);

        staticMapView = mapView;
        staticUser = user;

        try (FileInputStream ulazIni = new FileInputStream("dat/settings.ini")){
            Ini ini = new Ini();
            ini.load(ulazIni);
            unitType = (String) ini.getValue("Units", "unit");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            startButton.setText(Dictionary.get("StartButton"));
            beginning.setPromptText(Dictionary.get("Beginning"));
            end.setPromptText(Dictionary.get("End"));
            search.setText(Dictionary.get("Search"));
            addStop.setText(Dictionary.get("AddStop"));
            distanceLabel.setText(Dictionary.get("DistanceLabel"));
            speedLabel.setText(Dictionary.get("SpeedLabel"));
            avgSpeedLabel.setText(Dictionary.get("AvgSpeedLabel"));
            maxSpeedLabel.setText(Dictionary.get("MaxSpeedLabel"));
            saveRouteLabel.setText(Dictionary.get("SaveRouteLabel"));
            savePopup.setText(Dictionary.get("SavePopup"));
            cancelPopup.setText(Dictionary.get("cancel"));

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void mapInitializer() {
        mapView.setZoom(1);
        mapView.setCenter(0, 0);
    }

    @FXML
    private void onSearchButtonPress(){
        mapView.removeLayer(routeLayer);
        List<Waypoint> waypoints = new ArrayList<>();
        try {
            if (beginning.getText().equals("") || end.getText().equals("")){
                throw new FieldIsEmptyException("Make sure no waypoint fields are empty!");
            }
            waypoints.add(new Waypoint(beginning.getText(), Double.parseDouble(finalLocations.get(beginning.getText()).split(",")[0]), Double.parseDouble(finalLocations.get(beginning.getText()).split(",")[1])));
            for (TextField s: stops) {
                if (s.getText().equals("")){
                    throw new FieldIsEmptyException("Make sure no waypoint fields are empty!");
                }
                waypoints.add(new Waypoint(s.getText(), Double.parseDouble(finalLocations.get(s.getText()).split(",")[0]), Double.parseDouble(finalLocations.get(s.getText()).split(",")[1])));
            }
            waypoints.add(new Waypoint(end.getText(), Double.parseDouble(finalLocations.get(end.getText()).split(",")[0]), Double.parseDouble(finalLocations.get(end.getText()).split(",")[1])));

            route = new Route.RouteBuilder().name("Temp").waypoints(waypoints).userID(Database.getUserByUsername(user.username())).build();

            routeLayer = new DrawRoute(route);
            mapView.addLayer(routeLayer);
            mapView.setZoom(15);
            mapView.setCenter(waypoints.getFirst().getLat(), waypoints.getFirst().getLng());
        } catch (FieldIsEmptyException ex) {
            logger.error("Cannot create route because some waypoints are blank!", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error while trying to finde a route");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onAddStopButtonPress() {
        HBox hbox = new HBox();
        TextField textField = new TextField ();
        Button chooseOnMapButton = new Button("?");
        Button removeStopButton = new Button("x");

        textField.setOnKeyTyped(event -> updateList());
        textField.setOnMouseClicked(event -> hideList());

        chooseOnMapButton.setOnAction(event -> onChooseOnMapButtonPress(textField));
        removeStopButton.setOnAction(event -> onRemoveStopButtonPress(hbox));

        hbox.getChildren().addAll(textField, chooseOnMapButton, removeStopButton);

        textField.setPrefWidth(260);

        VBox.setMargin(hbox, new Insets(0, 30, 10, 30));
        vb.getChildren().add(vb.getChildren().size() - 3  , hbox);
        mapView.setPrefHeight(mapView.getPrefHeight() - 35);

        stops.add(textField);
        chooseOnMapButtons.add(chooseOnMapButton);
        removeStopButtons.add(removeStopButton);
    }

    @FXML
    private void updateList() {
        locations.clear();
        if (!runningUpdateThreads.isEmpty()){
            ((UpdateList) runningUpdateThreads.getFirst()).setStopFlag(true);
            runningUpdateThreads.removeFirst();
        }

        runningUpdateThreads.add(new UpdateList(mapView, results, locations));

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(runningUpdateThreads.getFirst());
        executorService.shutdown();
    }

    @FXML
    private void onStartButtonPress() {
        if (check) {
            ride = new Ride(user.username());
            ride.startRide(mapView, distanceTraveled, currentSpeed, averageSpeed, maxSpeed);
            try {
                startButton.setText(Dictionary.get("StopButton"));
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new RuntimeException(e);
            }
            check = false;
        }
        else {
            ride.stopTimeline();
            AlertLib alertLib = Native.loadLibrary("AlertLib", AlertLib.class);

            int result = alertLib.saveRideAlert();
            if (result == 1){
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    ride.setName(dtf.format(now));
                    Database.insertNewRideToDatabase(ride);
                    ChangeLog.log(new Log(dtf.format(now), "Ride added", "User " + user.username() + " saved a ride", user.username(), user.role()));
                } catch (SQLException | IOException ex) {
                    logger.error("Error writing to database", ex);
                } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                         InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
            check= true;
            try {
                startButton.setText(Dictionary.get("StartButton"));
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void onChooseOnMapButtonPress(TextField textField) {
        EventHandler<MouseEvent> doubleClick = new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2){
                        String coords = (mapView.getMapPosition(mouseEvent.getX(), mouseEvent.getY()).getLatitude() + "," + mapView.getMapPosition(mouseEvent.getX(), mouseEvent.getY()).getLongitude());
                        textField.setText(coords);
                        finalLocations.put(coords, coords);
                        mapView.removeEventHandler(mouseEvent.getEventType(), this);
                    }
                }
            }
        };

        mapView.addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClick);
    }

    @FXML
    private void onSaveRouteButtonPress() {
        try {
            if (route == null){
                throw new NoRouteException("The route couldn't be saved as there is no route.");
            }
            popup.setVisible(true);
        } catch (NoRouteException ex) {
            logger.error("Cannot save route as there is n route!", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save route");
            alert.setHeaderText("Couln't save route!");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void save(){
        if (saveRouteName.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save route");
            alert.setHeaderText("Error!");
            alert.setContentText("You need to write the name to save a route.");
            alert.showAndWait();
        }
        else {
            try {
                route.setName(saveRouteName.getText());
                Database.insertNewRouteToDatabase(route);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                ChangeLog.log(new Log(dtf.format(now), "Route added", "User " + user.username() + " saved a route", user.username(), user.role()));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Save route");
                alert.setHeaderText("Congrats!");
                alert.setContentText("You have succesfully saved a new route.");
                alert.showAndWait();
            } catch (SQLException | IOException ex) {
                logger.error("Error writing to database", ex);
            }
            popup.setVisible(false);
        }
    }

    @FXML
    private void cancel() {
        popup.setVisible(false);
    }

    private void onRemoveStopButtonPress(HBox hbox) {
        vb.getChildren().remove(hbox);
        mapView.setPrefHeight(mapView.getPrefHeight() + 35);
        stops.remove(hbox.getChildren().get(0));
        chooseOnMapButtons.remove(hbox.getChildren().get(1));
        removeStopButtons.remove(hbox.getChildren().get(2));
        finalLocations.remove(hbox.getChildren().get(0).toString());
    }

    @FXML
    private void onSettingsButtonPress() {
        try {
            FXMLLoader fxmlLoader;
            if (Objects.equals(user.role(), "Admin")){
                fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminMenuScreen.fxml"));
            }
            else {
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

    @FXML
    private void hideList(){
        results.setVisible(false);
    }

    public void drawRoute(String routeName) {
        try {
            List<Route> routes = Database.getRoutesByUsernameDB(staticUser.username());
            for (Route r: routes) {
                if (r.getName().equals(routeName)){
                    stops.clear();
                    finalLocations.clear();

                    List<Waypoint> waypointsList = r.getWaypoints();

                    for(int i = 0; i < waypointsList.size(); i++) {
                        if (i == 0){
                            beginning.setText(waypointsList.get(i).getName().toString());
                        }
                        else if (i == waypointsList.size() - 1){
                            end.setText(waypointsList.get(i).getName().toString());
                        }
                        else {
                            onAddStopButtonPress();
                            stops.getLast().setText(waypointsList.get(i).getName().toString());
                        }
                        finalLocations.put(waypointsList.get(i).getName().toString(), waypointsList.get(i).getLat() + "," + waypointsList.get(i).getLng());
                    }

                    routeLayer = new DrawRoute(r);
                    staticMapView.addLayer(routeLayer);
                    staticMapView.setZoom(15);
                    staticMapView.setCenter(r.getWaypoints().getFirst().getLat(), r.getWaypoints().getFirst().getLng());
                    break;
                }
            }
        } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}