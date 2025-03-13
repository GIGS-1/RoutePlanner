package hr.java.application.routeplanner;

import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.entity.*;
import hr.java.application.routeplanner.entity.Dictionary;
import hr.java.application.routeplanner.interfaces.EntityScreens;
import hr.java.application.routeplanner.records.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class AdminScreenController implements Initializable, EntityScreens {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    User currentUser;
    List<Route> routes = new ArrayList<>();
    List<Ride> rides = new ArrayList<>();
    List<User> users = new ArrayList<>();

    @FXML
    private VBox vb;
    @FXML
    private ImageView logo;
    @FXML
    private ImageView backButton;
    @FXML
    private ComboBox<String> entity;
    @FXML
    private Label title;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private TextField textField1;
    @FXML
    private TextField textField2;
    @FXML
    private Button searchButton;
    @FXML
    private Button addButton;
    @FXML
    private VBox popup;

    TableView<Route> routeTable = new TableView<>();
    TableColumn<Route, String> routeTableNameColumn = new TableColumn<>();
    TableColumn<Route, String> routeTableWaypointsColumn = new TableColumn<>();
    TableColumn<Route, String> routeTableUserColumn = new TableColumn<>();

    TableView<Ride> rideTable = new TableView<>();
    TableColumn<Ride, String> rideTableNameColumn = new TableColumn<>();
    TableColumn<Ride, String> rideTableRidePointsColumn = new TableColumn<>();
    TableColumn<Ride, Double> rideTableDistanceTraveledColumn = new TableColumn<>();
    TableColumn<Ride, Double> rideTableAvgSpeedColumn = new TableColumn<>();
    TableColumn<Ride, Double> rideTableMaxSpeedColumn = new TableColumn<>();
    TableColumn<Ride, String> rideTableTimeColumn = new TableColumn<>();

    TableView<User> userTable = new TableView<>();
    TableColumn<User, String> userTableNameColumn = new TableColumn<>();
    TableColumn<User, String> userTableRoleColumn = new TableColumn<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logo.setImage(new Image(new FileInputStream("img/logo.png")));
            backButton.setImage(new Image(new FileInputStream("img/back.png")));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        createRouteTable();
        createRideTable();
        createUserTable();

        currentUser = LoginScreenController.user;

        ObservableList<String> observableEntities = FXCollections.observableArrayList(new String[]{"Route", "Ride", "User"});
        entity.setItems(observableEntities);

        try {
            users = Database.getUsersDB();
        } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        title.setVisible(false);
        label1.setVisible(false);
        label2.setVisible(false);
        textField1.setVisible(false);
        textField2.setVisible(false);
        searchButton.setVisible(false);
        addButton.setVisible(false);
        routeTable.setVisible(false);
        rideTable.setVisible(false);
        userTable.setVisible(false);
        popup.setVisible(false);

        try {
            searchButton.setText(Dictionary.get("Search"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBackButtonPress() {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminMenuScreen.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
        } catch (IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }

        HelloApplication.getStage().setScene(scene);
        HelloApplication.getStage().show();
    }

    @FXML
    private void updateScreen() {
        if (entity.getSelectionModel().getSelectedItem().equals("Route")){
            vb.getChildren().remove(routeTable);
            vb.getChildren().remove(userTable);
            vb.getChildren().remove(rideTable);
            vb.getChildren().add(routeTable);
            drawRouteScreen();
        }
        else if (entity.getSelectionModel().getSelectedItem().equals("Ride")){
            vb.getChildren().remove(routeTable);
            vb.getChildren().remove(userTable);
            vb.getChildren().remove(rideTable);
            vb.getChildren().add(rideTable);
            drawRideScreen();
        }
        else {
            vb.getChildren().remove(routeTable);
            vb.getChildren().remove(userTable);
            vb.getChildren().remove(rideTable);
            vb.getChildren().add(userTable);
            drawUserScreen();
        }

        title.setVisible(true);
        label1.setVisible(true);
        label2.setVisible(true);
        textField1.setVisible(true);
        textField2.setVisible(true);
        searchButton.setVisible(true);
        addButton.setVisible(true);
    }

    @FXML
    private void onSearchButtonPress() {
        if (entity.getSelectionModel().getSelectedItem().equals("Route")){
            List<Route> routesFiltered = routes.stream().filter(r -> r.getName().toString().contains(textField1.getText())).toList();
            routesFiltered = routesFiltered.stream().filter(r -> r.getWaypoints().stream().map(NamedEntity::getName).reduce("", (str1, str2) -> str1 + " | " + str2).toString().contains(textField2.getText())).toList();

            ObservableList<Route> observableRoutesFiltered = FXCollections.observableArrayList(routesFiltered);
            routeTable.setItems(observableRoutesFiltered);
        }
        else if (entity.getSelectionModel().getSelectedItem().equals("Ride")){
            List<Ride> ridesFiltered = rides.stream().filter(r -> r.getName().toString().contains(textField1.getText())).toList();

            if (MainScreenController.unitType.equals("Metric")) {
                ridesFiltered = ridesFiltered.stream().filter(r -> r.getDistanceTraveled().toString().contains(textField2.getText())).toList();
            }
            else  {
                ridesFiltered = ridesFiltered.stream().filter(r -> String.valueOf(Math.floor(r.getDistanceTraveled()/1.609*100)/100).contains(textField2.getText())).toList();
            }

            ObservableList<Ride> observableRidesFiltered = FXCollections.observableArrayList(ridesFiltered);
            rideTable.setItems(observableRidesFiltered);
        }
        else {
            List<User> usersFiltered = users.stream().filter(u -> u.username().contains(textField1.getText())).toList();
            usersFiltered = usersFiltered.stream().filter(u -> u.role().contains(textField2.getText())).toList();

            ObservableList<User> observableUsersFiltered = FXCollections.observableArrayList(usersFiltered);
            userTable.setItems(observableUsersFiltered);
        }
    }

    @FXML
    private void onAddButtonPress() {
        popup.getChildren().clear();

        if (entity.getSelectionModel().getSelectedItem().equals("Route")){
            popup.setPrefHeight(260);

            Label title = new Label("Add a new route");
            Label nameLabel = new Label("Name:");
            Label waypointsLabel = new Label("Waypoints:");
            Label userLabel = new Label("User:");
            TextField nameTextField = new TextField();
            TextField waypointsTextField = new TextField();
            ComboBox<String> userComboBox = new ComboBox<>();
            Button addBtn = new Button("Add");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            addBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(waypointsTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(addBtn, new Insets(20, 0, 10, 0));

            ObservableList<String>  observableUsers = FXCollections.observableArrayList(users.stream().map(User::username).toList());
            userComboBox.setItems(observableUsers);

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            addBtn.setOnAction(event -> {
                if (!nameTextField.getText().isEmpty() && !waypointsTextField.getText().isEmpty()){
                    List<Waypoint> waypoints = new ArrayList<>();
                    String[] strings = waypointsTextField.getText().split(";");
                    for (String s: strings) {
                        waypoints.add(new Waypoint(s, Double.parseDouble(s.split(",")[0]), Double.parseDouble(s.split(",")[1])));
                    }

                    for (User user: users) {
                        if (userComboBox.getSelectionModel().getSelectedItem().equals(user.username())){
                            try {
                                Database.insertNewRouteToDatabase(new Route.RouteBuilder().name(nameTextField.getText()).waypoints(waypoints).userID(Database.getUserByUsername(user.username())).build());
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
                                ChangeLog.log(new Log(dtf.format(now), "Route added", "Added a route to user " + user.username(), LoginScreenController.user.username(), "Admin"));
                            } catch (SQLException | IOException ex) {
                                logger.error("Error writing to database", ex);
                            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                                     InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Add route");
                    alert.setHeaderText("Congrats!");
                    alert.setContentText("You have succesfully added a new route.");
                    alert.showAndWait();
                    popup.setVisible(false);
                    updateScreen();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save route");
                    alert.setHeaderText("Error saving route");
                    alert.setContentText("Please make sure all fields are filled before adding");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, waypointsLabel, waypointsTextField, userLabel, userComboBox, addBtn, cancelBtn);
        }
        else if (entity.getSelectionModel().getSelectedItem().equals("Ride")){
            popup.setPrefHeight(430);

            Label title = new Label("Edit a ride");
            Label nameLabel = new Label("Name:");
            Label distanceLabel = new Label("Distance:");
            Label avgSpeedLabel = new Label("Average speed:");
            Label maxSpeedLabel = new Label("Max speed:");
            Label ridePointsLabel = new Label("Ride points:");
            Label userLabel = new Label("User:");

            TextField nameTextField = new TextField();
            TextField ridePointsTextField = new TextField();
            TextField distanceTextField = new TextField();
            TextField avgSpeedTextField = new TextField();
            TextField maxSpeedTextField = new TextField();
            ComboBox<String> userComboBox = new ComboBox<>();

            Button saveBtn = new Button("Add");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            saveBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(ridePointsTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(distanceTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(avgSpeedTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(maxSpeedTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(userComboBox, new Insets(0, 30, 10, 30));
            VBox.setMargin(saveBtn, new Insets(0, 0, 10, 0));

            ObservableList<String>  observableUsers = FXCollections.observableArrayList(users.stream().map(User::username).toList());
            userComboBox.setItems(observableUsers);

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            saveBtn.setOnAction(event -> {
                if (!nameTextField.getText().isEmpty() && !ridePointsTextField.getText().isEmpty() && !distanceTextField.getText().isEmpty() && !avgSpeedTextField.getText().isEmpty() && !maxSpeedTextField.getText().isEmpty()){
                    for (User user: users) {
                        if (userComboBox.getSelectionModel().getSelectedItem().equals(user.username())){
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();
                            ChangeLog.log(new Log(dtf.format(now), "Ride added", "Added a ride to user " + user.username(), LoginScreenController.user.username(), "Admin"));
                            try {
                                if (MainScreenController.unitType.equals("Metric")){
                                    Database.insertNewRideToDatabase(new Ride(nameTextField.getText(), ridePointsTextField.getText(), Double.parseDouble(distanceTextField.getText()), Double.valueOf(0), Double.parseDouble(avgSpeedTextField.getText()), Double.parseDouble(maxSpeedTextField.getText()), user.username()));
                                }
                                else {
                                    Database.insertNewRideToDatabase(new Ride(nameTextField.getText(), ridePointsTextField.getText(), Math.floor(Double.parseDouble(distanceTextField.getText()) * 1.609 * 100) / 100, Double.valueOf(0), Math.floor(Double.parseDouble(avgSpeedTextField.getText()) * 1.609 * 100) / 100, Math.floor(Double.parseDouble(maxSpeedTextField.getText()) * 1.609 * 100) / 100, user.username()));
                                }
                            } catch (SQLException | IOException ex) {
                                logger.error("Error writing to database", ex);
                            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                                     InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Add ride");
                    alert.setHeaderText("Congrats!");
                    alert.setContentText("You have succesfully added a new ride.");
                    alert.showAndWait();
                    popup.setVisible(false);
                    updateScreen();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save route");
                    alert.setHeaderText("Error saving route");
                    alert.setContentText("Please make sure all fields are filled before adding");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, ridePointsLabel, ridePointsTextField, distanceLabel, distanceTextField, avgSpeedLabel, avgSpeedTextField, maxSpeedLabel, maxSpeedTextField, userLabel, userComboBox, saveBtn, cancelBtn);
        }
        else {
            popup.setPrefHeight(260);

            Label title = new Label("Add user");
            Label nameLabel = new Label("Username:");
            Label passwordLabel = new Label("Password:");
            Label roleLabel = new Label("Role:");

            TextField nameTextField = new TextField();
            TextField passwordTextField = new TextField();
            TextField roleTextField = new TextField();

            Button saveBtn = new Button("Save");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            saveBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(passwordTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(roleTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(saveBtn, new Insets(0, 0, 10, 0));

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            saveBtn.setOnAction(event -> {
                Boolean check = true;
                if (!nameTextField.getText().isEmpty() && !passwordTextField.getText().isEmpty() && !roleTextField.getText().isEmpty() && passwordTextField.getText().length() >= 8){
                    for (User u: users) {
                        if (u.username().equals(nameTextField.getText())){
                            check = false;
                        }
                    }
                    if (check){
                        try {
                            MessageDigest md5 = MessageDigest.getInstance("MD5");
                            byte[] md5Hash = md5.digest((passwordTextField.getText()).getBytes(StandardCharsets.UTF_8));
                            String md5HashedPassword = Base64.getEncoder().encodeToString(md5Hash);

                            Random ran = new Random();
                            String x = String.valueOf(ran.nextInt(10));

                            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                            byte[] sha256Hash = sha256.digest((md5HashedPassword + passwordTextField.getText() + x).getBytes(StandardCharsets.UTF_8));
                            String hashedPassword = Base64.getEncoder().encodeToString(sha256Hash);

                            User user = new User(nameTextField.getText(), hashedPassword, roleTextField.getText());

                            Database.insertNewUserToDatabase(user);
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();
                            ChangeLog.log(new Log(dtf.format(now), "User added", "Added a new user: " + user.username(), LoginScreenController.user.username(), "Admin"));

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Add user");
                            alert.setHeaderText("Congrats!");
                            alert.setContentText("You have succesfully added a userdata" + ".");
                            alert.showAndWait();
                            popup.setVisible(false);
                            updateScreen();
                        } catch (NoSuchAlgorithmException | SQLException | IOException e) {
                            logger.error("Error communicatig to database", e);
                            Alert alert1 = new Alert(Alert.AlertType.ERROR);
                            alert1.setTitle("Edit user data");
                            alert1.setHeaderText("Error editing");
                            alert1.setContentText("There was an error while editing, please try again!");
                            alert1.showAndWait();
                        } catch (NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException |
                                 BadPaddingException | InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Edit user data");
                    alert.setHeaderText("Error editing");
                    alert.setContentText("Please make sure all fields are filled before adding and password is at least 8 characters long");
                    alert.showAndWait();
                }
                if (!check) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Edit user data");
                    alert.setHeaderText("Error editing");
                    alert.setContentText("User with that name already exists");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, passwordLabel, passwordTextField, roleLabel, roleTextField, saveBtn, cancelBtn);
        }



        popup.setVisible(true);
    }

    @Override
    public void drawRouteScreen() {
        try {
            rideTable.setVisible(false);
            userTable.setVisible(false);

            title.setText(Dictionary.get("Route"));
            label1.setText(Dictionary.get("NameLabel"));
            label2.setText(Dictionary.get("WaypointsLabel"));

            routes.clear();

            routes = Database.getRoutesDB();

            ObservableList<Route> observableRoutes = FXCollections.observableArrayList(routes);
            routeTable.setVisible(true);
            routeTable.setItems(observableRoutes);
        } catch (NoSuchMethodError ex){
            logger.error(ex.getMessage(), ex);
        } catch (SQLException | IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawRideScreen() {
        try {
            routeTable.setVisible(false);
            userTable.setVisible(false);

            title.setText("Ride");
            label1.setText("Name:");
            label2.setText("Distance:");

            title.setText(Dictionary.get("Ride"));
            label1.setText(Dictionary.get("NameLabel"));
            label2.setText(Dictionary.get("DistanceLabel1"));

            rides.clear();

            rides = Database.getRidesDB();

            rideTable.setVisible(true);

            ObservableList<Ride> observableRides = FXCollections.observableArrayList(rides);
            rideTable.setItems(observableRides);
        } catch (SQLException | IOException ex) {
            logger.error("Error reading from database", ex);
        } catch (NoSuchMethodError ex){
            logger.error(ex.getMessage(), ex);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException | ParserConfigurationException |
                 SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawUserScreen() {
        try {
            routeTable.setVisible(false);
            rideTable.setVisible(false);

            title.setText(Dictionary.get("User"));
            label1.setText(Dictionary.get("NameLabel"));
            label2.setText(Dictionary.get("RoleLabel"));

            users.clear();

            users = Database.getUsersDB();

            userTable.setVisible(true);
            ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
            userTable.setItems(observableUsers);
        }
        catch (IOException ex) {
            logger.error("Couldn't read from input file", ex);
        } catch (SQLException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException | ParserConfigurationException |
                 SAXException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodError ex){
            logger.error(ex.getMessage(), ex);
        }
    }

    private void createRouteTable() {
        routeTableNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getName()).asString());
        routeTableWaypointsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getWaypoints().stream().map(NamedEntity::getName).reduce("", (str1, str2) -> str1 + " | " + str2)).asString());
        routeTableUserColumn.setCellValueFactory(param -> {
            try {
                return new ReadOnlyObjectWrapper<>(Database.getUserByID(param.getValue().getUserID()));
            } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            routeTableNameColumn.setText(Dictionary.get("Name"));
            routeTableWaypointsColumn.setText(Dictionary.get("Waypoints"));
            routeTableUserColumn.setText(Dictionary.get("Username"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        routeTable.getColumns().add(routeTableNameColumn);
        routeTable.getColumns().add(routeTableWaypointsColumn);
        routeTable.getColumns().add(routeTableUserColumn);

        routeTable.setPrefHeight(435);

        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(event -> {
            cm.hide();
            onEditButtonPress(routeTable.getSelectionModel().getSelectedItem());
        });
        cm.getItems().add(edit);
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> {
            cm.hide();
            for (User user: users) {
                try {
                    if (routeTable.getSelectionModel().getSelectedItem().getUserID() == Database.getUserByUsername(user.username())){
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Confirm changes");
                        alert.setContentText("Are you sure you want to delete this route?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK){
                            try {
                                Database.removeRoute(routeTable.getSelectionModel().getSelectedItem());
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
                                ChangeLog.log(new Log(dtf.format(now), "Removed a route", "Removed route " + routeTable.getSelectionModel().getSelectedItem().getName(), LoginScreenController.user.username(), "Admin"));
                            } catch (SQLException | IOException ex) {
                                logger.error("Error deleting from database database", ex);
                            }
                            routes.remove(routeTable.getSelectionModel().getSelectedItem());
                        }
                    }
                } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException |
                         InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
            updateScreen();
        });
        cm.getItems().add(delete);

        routeTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if(t.getButton() == MouseButton.SECONDARY) {
                    cm.show(routeTable, t.getScreenX(), t.getScreenY());
                }
            }
        });
    }

    private void createRideTable() {
        rideTableNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getName()).asString());
        rideTableRidePointsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getRidePointsString()));
        rideTableTimeColumn.setCellValueFactory(pram -> new ReadOnlyObjectWrapper<>(String.format("%02d:%02d:%02d", (int)(pram.getValue().getDistanceTraveled()/pram.getValue().getAvgSpeed()), (int)(pram.getValue().getDistanceTraveled()/pram.getValue().getAvgSpeed()*60%60), Math.round(pram.getValue().getDistanceTraveled()/pram.getValue().getAvgSpeed()*60*60%60))));

        if (MainScreenController.unitType.equals("Metric")) {
            rideTableDistanceTraveledColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getDistanceTraveled()));
            rideTableAvgSpeedColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getAvgSpeed()));
            rideTableMaxSpeedColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMaxSpeed()));
        }
        else {
            rideTableDistanceTraveledColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Math.floor(param.getValue().getDistanceTraveled()/1.609*100)/100));
            rideTableAvgSpeedColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Math.floor(param.getValue().getAvgSpeed()/1.609*100)/100));
            rideTableMaxSpeedColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Math.floor(param.getValue().getMaxSpeed()/1.609*100)/100));
        }

        try {
            rideTableNameColumn.setText(Dictionary.get("Name"));
            rideTableRidePointsColumn.setText(Dictionary.get("RidePoints"));
            rideTableDistanceTraveledColumn.setText(Dictionary.get("DistanceLabel"));
            rideTableAvgSpeedColumn.setText(Dictionary.get("AvgSpeedLabel"));
            rideTableMaxSpeedColumn.setText(Dictionary.get("MaxSpeedLabel"));
            rideTableTimeColumn.setText(Dictionary.get("Time"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        rideTable.getColumns().add(rideTableNameColumn);
        rideTable.getColumns().add(rideTableDistanceTraveledColumn);
        rideTable.getColumns().add(rideTableAvgSpeedColumn);
        rideTable.getColumns().add(rideTableMaxSpeedColumn);
        rideTable.getColumns().add(rideTableTimeColumn);
        rideTable.getColumns().add(rideTableRidePointsColumn);

        rideTable.setPrefHeight(435);

        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(event -> {
            cm.hide();
            onEditButtonPress(rideTable.getSelectionModel().getSelectedItem());
        });
        cm.getItems().add(edit);
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> {
            cm.hide();
            for (User user: users) {
                if (rideTable.getSelectionModel().getSelectedItem().getUsername().equals(user.username())){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("Confirm changes");
                    alert.setContentText("Are you sure you want to delete this ride?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime now = LocalDateTime.now();
                        ChangeLog.log(new Log(dtf.format(now), "Removed a ride", "Removed ride " + rideTable.getSelectionModel().getSelectedItem().getName(), LoginScreenController.user.username(), "Admin"));
                        try {
                            Database.removeRide(rideTable.getSelectionModel().getSelectedItem());
                            rides.remove(rideTable.getSelectionModel().getSelectedItem());
                        } catch (SQLException | IOException ex) {
                            logger.error("Error deleting from database database", ex);
                        }
                        break;
                    }

                }
            }
            updateScreen();
        });
        cm.getItems().add(delete);

        rideTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if(t.getButton() == MouseButton.SECONDARY) {
                    cm.show(rideTable, t.getScreenX(), t.getScreenY());
                }
            }
        });
    }

    private void createUserTable() {
        userTableNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().username()));
        userTableRoleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().role()));

        try {
            userTableNameColumn.setText(Dictionary.get("Username"));
            userTableRoleColumn.setText(Dictionary.get("Role"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        userTable.getColumns().add(userTableNameColumn);
        userTable.getColumns().add(userTableRoleColumn);

        userTable.setPrefHeight(435);

        ContextMenu cm = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(event -> {
            cm.hide();
            onEditButtonPress(userTable.getSelectionModel().getSelectedItem());
        });
        cm.getItems().add(edit);
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> {
            cm.hide();
            for (User user: users) {
                if (userTable.getSelectionModel().getSelectedItem().username().equals(user.username())){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("Confirm changes");
                    alert.setContentText("Are you sure you want to delete this user?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        try {
                            for (Route r: Database.getRoutesByUsernameDB(userTable.getSelectionModel().getSelectedItem().username())) {
                                Database.removeRoute(r);
                            }
                            for (Ride r: Database.getRidesByUsernameDB(userTable.getSelectionModel().getSelectedItem().username())) {
                                Database.removeRide(r);
                            }
                            Database.removeUser(userTable.getSelectionModel().getSelectedItem());
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime now = LocalDateTime.now();
                            ChangeLog.log(new Log(dtf.format(now), "Removed a user", "Removed user " + userTable.getSelectionModel().getSelectedItem().username(), LoginScreenController.user.username(), "Admin"));
                        } catch (SQLException | IOException ex) {
                            logger.error("Error deleting from database database", ex);
                        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                        users.remove(user);
                        break;
                    }
                }
            }
            updateScreen();
        });
        cm.getItems().add(delete);

        userTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if(t.getButton() == MouseButton.SECONDARY) {
                    cm.show(userTable, t.getScreenX(), t.getScreenY());
                }
            }
        });
    }

    private void onEditButtonPress(Object selectedItem) {
        popup.getChildren().clear();
        popup.setPrefHeight(200);

        if (entity.getSelectionModel().getSelectedItem().equals("Route")){
            Route route = (Route) selectedItem;
            Label title = new Label("Edit a route");
            Label nameLabel = new Label("Name:");
            Label waypointsLabel = new Label("Waypoints:");
            TextField nameTextField = new TextField((String) route.getName());
            String waypointsString = "";
            for (Waypoint w: route.getWaypoints()) {
                waypointsString += ";" + w.getLat() + ", " + w.getLng();
            }
            TextField waypointsTextField = new TextField(waypointsString.substring(1));
            Button saveBtn = new Button("Save");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            saveBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(waypointsTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(saveBtn, new Insets(0, 0, 10, 0));

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            String finalWaypointsString = waypointsString;
            saveBtn.setOnAction(event -> {
                if (!nameTextField.getText().isEmpty() && !waypointsTextField.getText().isEmpty()){
                    List<Waypoint> waypoints = new ArrayList<>();
                    String[] strings = waypointsTextField.getText().split(";");
                    for (String s: strings) {
                        waypoints.add(new Waypoint(s, Double.parseDouble(s.split(",")[0]), Double.parseDouble(s.split(",")[1])));
                    }

                    for (User user: users) {
                        try {
                            if (route.getUserID() == Database.getUserByUsername(user.username())){
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Confirmation Dialog");
                                alert.setHeaderText("Confirm changes");
                                alert.setContentText("Are you sure you want to save the changes?");

                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    Route newRoute = new Route.RouteBuilder().name(nameTextField.getText()).waypoints(waypoints).userID(Database.getUserByUsername(user.username())).build();

                                    String newWaypointsString = "";
                                    for (Waypoint w: route.getWaypoints()) {
                                        newWaypointsString += ";" + w.getLat() + ", " + w.getLng();
                                    }

                                    try {
                                        Database.editRoute(route, newRoute);
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        LocalDateTime now = LocalDateTime.now();
                                        ChangeLog.log(new Log(dtf.format(now), "Edited a route", route.getName() + " | " + finalWaypointsString.substring(1) + " -> " + newRoute.getName() + " | " + newWaypointsString.substring(1), LoginScreenController.user.username(), "Admin"));
                                    } catch (SQLException | IOException ex) {
                                        logger.error("Error communicatig to database", ex);
                                    }
                                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                                    alert1.setTitle("Edit route");
                                    alert1.setHeaderText("Congrats!");
                                    alert1.setContentText("You have succesfully save the changes.");
                                    alert1.showAndWait();
                                    break;
                                }
                            }
                        } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                                 NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException |
                                 InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    popup.setVisible(false);
                    updateScreen();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save route");
                    alert.setHeaderText("Error saving route");
                    alert.setContentText("Please make sure all fields are filled before adding");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, waypointsLabel, waypointsTextField, saveBtn, cancelBtn);
        }
        else if (entity.getSelectionModel().getSelectedItem().equals("Ride")){
            popup.setPrefHeight(370);
            Ride ride = (Ride) selectedItem;

            Label title = new Label("Edit a ride");
            Label nameLabel = new Label("Name:");
            Label distanceLabel = new Label("Distance:");
            Label avgSpeedLabel = new Label("Average speed:");
            Label maxSpeedLabel = new Label("Max speed:");
            Label ridePointsLabel = new Label("Ride points:");

            TextField nameTextField = new TextField((String) ride.getName());
            TextField ridePointsTextField = new TextField(ride.getRidePointsString());
            TextField distanceTextField;
            TextField avgSpeedTextField;
            TextField maxSpeedTextField;
            if (MainScreenController.unitType.equals("Metric")) {
                distanceTextField = new TextField(ride.getDistanceTraveled().toString());
                avgSpeedTextField = new TextField(ride.getAvgSpeed().toString());
                maxSpeedTextField = new TextField(ride.getMaxSpeed().toString());
            }
            else {
                distanceTextField = new TextField(String.valueOf(Math.floor(ride.getDistanceTraveled()/1.609*100)/100));
                avgSpeedTextField = new TextField(String.valueOf(Math.floor(ride.getAvgSpeed()/1.609*100)/100));
                maxSpeedTextField = new TextField(String.valueOf(Math.floor(ride.getMaxSpeed()/1.609*100)/100));
            }

            Button saveBtn = new Button("Save");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            saveBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(ridePointsTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(distanceTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(avgSpeedTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(maxSpeedTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(saveBtn, new Insets(0, 0, 10, 0));

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            saveBtn.setOnAction(event -> {
                if (!nameTextField.getText().isEmpty() && !ridePointsTextField.getText().isEmpty() && !distanceTextField.getText().isEmpty() && !avgSpeedTextField.getText().isEmpty() && !maxSpeedTextField.getText().isEmpty()){
                    for (User user: users) {
                        if (ride.getUsername().equals(user.username())){
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirmation Dialog");
                            alert.setHeaderText("Confirm changes");
                            alert.setContentText("Are you sure you want to save the changes?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK){
                                Ride newRide;
                                if (MainScreenController.unitType.equals("Metric")){
                                    newRide = new Ride(nameTextField.getText(), ridePointsTextField.getText(), Double.parseDouble(distanceTextField.getText()), ride.getSpeed(), Double.parseDouble(avgSpeedTextField.getText()), Double.parseDouble(maxSpeedTextField.getText()), ride.getUsername());
                                }
                                else {
                                    newRide = new Ride(nameTextField.getText(), ridePointsTextField.getText(), Math.floor(Double.parseDouble(distanceTextField.getText()) * 1.609 * 100) / 100, Math.floor(ride.getSpeed() * 1.609 * 100) / 100, Math.floor(Double.parseDouble(avgSpeedTextField.getText()) * 1.609 * 100) / 100, Math.floor(Double.parseDouble(maxSpeedTextField.getText()) * 1.609 * 100) / 100, ride.getUsername());
                                }

                                try {
                                    Database.editRide(ride, newRide);
                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    LocalDateTime now = LocalDateTime.now();
                                    ChangeLog.log(new Log(dtf.format(now), "Edited a ride", ride.getName() + " | " + ride.getDistanceTraveled() + " | " + ride.getAvgSpeed() + " | " + ride.getMaxSpeed() + " | " + ride.getRidePointsString() + " -> " + newRide.getName() + " | " + newRide.getDistanceTraveled() + " | " + newRide.getAvgSpeed() + " | " + newRide.getMaxSpeed() + " | " + newRide.getRidePointsString(), LoginScreenController.user.username(), "Admin"));
                                } catch (SQLException | IOException ex) {
                                    logger.error("Error communicatig to database", ex);
                                } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                                         InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                                    throw new RuntimeException(e);
                                }
                                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                                alert1.setTitle("Add ride");
                                alert1.setHeaderText("Congrats!");
                                alert1.setContentText("You have succesfully added a new ride.");
                                alert1.showAndWait();
                                break;
                            }
                        }
                    }
                    popup.setVisible(false);
                    updateScreen();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Save route");
                    alert.setHeaderText("Error saving route");
                    alert.setContentText("Please make sure all fields are filled before adding");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, ridePointsLabel, ridePointsTextField, distanceLabel, distanceTextField, avgSpeedLabel, avgSpeedTextField, maxSpeedLabel, maxSpeedTextField, saveBtn, cancelBtn);
        }
        else {
            popup.setPrefHeight(270);
            User selectedeUser = (User) selectedItem;

            Label title = new Label("Edit user data");
            Label nameLabel = new Label("Username:");
            Label passwordLabel = new Label("Password:");
            Label roleLabel = new Label("Role:");

            TextField nameTextField = new TextField(selectedeUser.username());
            TextField passwordTextField = new TextField();
            TextField roleTextField = new TextField(selectedeUser.role());

            Button saveBtn = new Button("Save");
            Button cancelBtn = new Button("Cancel");

            title.setStyle("-fx-font-size:18px");
            saveBtn.setPrefWidth(70);
            cancelBtn.setPrefWidth(70);
            VBox.setMargin(nameTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(passwordTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(roleTextField, new Insets(0, 30, 10, 30));
            VBox.setMargin(saveBtn, new Insets(0, 0, 10, 0));

            cancelBtn.setOnAction(event -> {popup.setVisible(false);});
            saveBtn.setOnAction(event -> {
                Boolean check = true;
                if (!nameTextField.getText().isEmpty() && !passwordTextField.getText().isEmpty() && !roleTextField.getText().isEmpty() && passwordTextField.getText().length() >= 8){
                    for (User u: users) {
                        if (!Objects.equals(u.username(), selectedeUser.username())){
                            if (u.username().equals(nameTextField.getText())){
                                check = false;
                            }
                        }
                    }
                    if (check){
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Confirm changes");
                        alert.setContentText("Are you sure you want to save the changes?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK){
                            try {
                                MessageDigest md5 = MessageDigest.getInstance("MD5");
                                byte[] md5Hash = md5.digest((passwordTextField.getText()).getBytes(StandardCharsets.UTF_8));
                                String md5HashedPassword = Base64.getEncoder().encodeToString(md5Hash);

                                Random ran = new Random();
                                String x = String.valueOf(ran.nextInt(10));

                                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                                byte[] sha256Hash = sha256.digest((md5HashedPassword + passwordTextField.getText() + x).getBytes(StandardCharsets.UTF_8));
                                String hashedPassword = Base64.getEncoder().encodeToString(sha256Hash);

                                User user = new User(nameTextField.getText(), hashedPassword, roleTextField.getText());

                                Database.editUser(selectedeUser, user);
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
                                ChangeLog.log(new Log(dtf.format(now), "Edited a user", selectedeUser.username() + " | " + selectedeUser.password() + " | " + selectedeUser.role() + " -> " + user.username() + " | " + user.password().hashCode() + " | " + user.role(), LoginScreenController.user.username(), "Admin"));

                                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                                alert1.setTitle("Edit user data");
                                alert1.setHeaderText("Congrats!");
                                alert1.setContentText("You have succesfully edited user data.");
                                alert1.showAndWait();
                            } catch (NoSuchAlgorithmException | SQLException | IOException e) {
                                logger.error("Error communicatig to database", e);
                                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                                alert1.setTitle("Edit user data");
                                alert1.setHeaderText("Error editing");
                                alert1.setContentText("There was an error while editing, please try again!");
                                alert1.showAndWait();
                            } catch (NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException |
                                     BadPaddingException | InvalidKeyException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        popup.setVisible(false);
                        updateScreen();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Edit user data");
                    alert.setHeaderText("Error editing");
                    alert.setContentText("Please make sure all fields are filled before adding and pasword is at least 8 characters long");
                    alert.showAndWait();
                }
                if (!check) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Edit user data");
                    alert.setHeaderText("Error editing");
                    alert.setContentText("User with that name already exists");
                    alert.showAndWait();
                }
            });

            popup.getChildren().addAll(title, nameLabel, nameTextField, passwordLabel, passwordTextField, roleLabel, roleTextField, saveBtn, cancelBtn);
        }

        popup.setVisible(true);
    }
}