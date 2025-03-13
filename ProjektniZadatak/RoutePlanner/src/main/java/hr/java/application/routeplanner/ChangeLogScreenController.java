package hr.java.application.routeplanner;

import hr.java.application.routeplanner.entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public final class ChangeLogScreenController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private List<Log> logs;

    @FXML
    private ImageView logo;
    @FXML
    private ImageView backButton;

    @FXML
    TableView<Log> logTable = new TableView<Log>();

    @FXML
    TableColumn<Log, String> logTableDateColumn = new TableColumn<>();
    @FXML
    TableColumn<Log, String> logTableTitleColumn = new TableColumn<>();
    @FXML
    TableColumn<Log, String> logTableMessageColumn = new TableColumn<>();
    @FXML
    TableColumn<Log, String> logTableUserColumn = new TableColumn<>();
    @FXML
    TableColumn<Log, String> logTableRoleColumn = new TableColumn<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logo.setImage(new Image(new FileInputStream("img/logo.png")));
            backButton.setImage(new Image(new FileInputStream("img/back.png")));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        logTableDateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getDate()));
        logTableTitleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTitle()));
        logTableMessageColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMessage()));
        logTableUserColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getUser()));
        logTableRoleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getRole()));

        logs = ChangeLog.readLogs();
        ObservableList<Log> observableLogs = FXCollections.observableArrayList(logs);
        logTable.setItems(observableLogs);
    }

    @FXML
    private void onBackButtonPress() {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminMenuScreen.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }
}