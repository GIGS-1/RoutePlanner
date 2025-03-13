package hr.java.application.routeplanner;

import com.sun.jna.Native;
import hr.java.application.routeplanner.entity.RSA;
import hr.java.application.routeplanner.interfaces.WinRegLib;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.prefs.Preferences;

import java.io.*;

public class HelloApplication extends Application {

    public static Stage mainStage;
    WinRegLib winRegLib = Native.loadLibrary("WinRegLib", WinRegLib.class);

    @Override
    public void start(Stage stage) throws IOException {

        Path path = Paths.get("dat/public.key");
        if (!Files.exists(path)) {
            try{
                RSA.generateKeys();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }

        mainStage = stage;

        mainStage.setX(winRegLib.Get('x'));
        mainStage.setY(winRegLib.Get('y'));

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), winRegLib.Get('w'), winRegLib.Get('h'));

        stage.setTitle("Route Planner");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getStage(){
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop(){
        winRegLib.Set('x', (int) mainStage.getX());
        winRegLib.Set('y', (int) mainStage.getY());
        winRegLib.Set('w', (int) mainStage.getScene().getWidth());
        winRegLib.Set('h', (int) mainStage.getScene().getHeight());
    }
}