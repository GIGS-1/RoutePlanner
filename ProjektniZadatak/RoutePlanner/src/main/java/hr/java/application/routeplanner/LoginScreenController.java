package hr.java.application.routeplanner;

import com.sun.jna.Native;
import hr.java.application.routeplanner.entity.AES;
import hr.java.application.routeplanner.entity.Database;
import hr.java.application.routeplanner.entity.Dictionary;
import hr.java.application.routeplanner.entity.Route;
import hr.java.application.routeplanner.interfaces.LoggerLib;
import hr.java.application.routeplanner.records.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

public class LoginScreenController {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    public static User user;
    public static String newUsername = "";

    @FXML
    private Label message;
    @FXML
    private ImageView logo;
    @FXML
    private TextField usernameIn;
    @FXML
    private PasswordField passwordIn;
    @FXML
    private Button logInButton;
    @FXML
    private Label createAccountButton;

    public void initialize(){
        try {
            logo.setImage(new Image(new FileInputStream(new File("img/logo.png"))));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        if (!Objects.equals(newUsername, "")) {
            usernameIn.setText(newUsername);
            newUsername = "";
        }

        try {
            logInButton.setText(Dictionary.get("logInButton"));
            createAccountButton.setText(Dictionary.get("createAccountButton"));
            usernameIn.setPromptText(Dictionary.get("usernameIn"));
            passwordIn.setPromptText(Dictionary.get("passwordIn"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onLoginButtonClick() {
        logger.info("Starting Application!");

        try {
            List<User> users = Database.getUsersDB();
            boolean userCheck = false;
            boolean passCheck = false;

            for (User u: users) {
                if (u.username().equals(usernameIn.getText())){
                    userCheck = true;

                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    byte[] md5Hash = md5.digest((passwordIn.getText()).getBytes(StandardCharsets.UTF_8));
                    String md5HashedPassword = Base64.getEncoder().encodeToString(md5Hash);

                    for (int i = 0; i < 10; i++){
                        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                        byte[] sha256Hash = sha256.digest((md5HashedPassword + passwordIn.getText() + String.valueOf(i)).getBytes(StandardCharsets.UTF_8));
                        String hashedPassword = Base64.getEncoder().encodeToString(sha256Hash);

                        if (Objects.equals(u.password(), hashedPassword)){
                            passCheck = true;
                            user = u;
                            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainScreen.fxml"));
                            Scene scene = null;
                            try {
                                scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
                                HelloApplication.getStage().setScene(scene);
                                HelloApplication.getStage().show();
                            } catch (RuntimeException | IOException ex) {
                                logger.error("Couldn't load new scene", ex);
                            }
                            break;
                        }
                    }
                }
            }
            if (!userCheck){
                message.setText(Dictionary.get("LoginScreenMessage1").replace("#", usernameIn.getText()));
            }
            else if (!passCheck){
                message.setText(Dictionary.get("LoginScreenMessage2"));
            }
        }
        catch (IOException ex) {
            logger.error("Couldn't read from input file", ex);
        } catch (SQLException | ParserConfigurationException | SAXException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    protected void onCreateAccoutnClick() {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("createAccountScreen.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }

        HelloApplication.getStage().setScene(scene);
        HelloApplication.getStage().show();
    }
}