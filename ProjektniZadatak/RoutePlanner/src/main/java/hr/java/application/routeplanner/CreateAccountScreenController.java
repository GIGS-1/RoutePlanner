package hr.java.application.routeplanner;

import com.sun.jna.Native;
import hr.java.application.routeplanner.entity.Database;
import hr.java.application.routeplanner.entity.Dictionary;
import hr.java.application.routeplanner.exceptions.PasswordTooShortException;
import hr.java.application.routeplanner.exceptions.UsernameAlreadyExistsException;
import hr.java.application.routeplanner.interfaces.AlertLib;
import hr.java.application.routeplanner.records.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class CreateAccountScreenController {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);

    @FXML
    private Label message;
    @FXML
    private ImageView logo;
    @FXML
    private TextField usernameIn;
    @FXML
    private PasswordField passwordIn;
    @FXML
    private Label mainText;
    @FXML
    private Label cancel;
    @FXML
    private Button create;

    public void initialize(){
        try {
            logo.setImage(new Image(new FileInputStream(new File("img/logo.png"))));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        try {
            mainText.setText(Dictionary.get("mainText"));
            cancel.setText(Dictionary.get("cancel"));
            create.setText(Dictionary.get("create"));
            usernameIn.setPromptText(Dictionary.get("usernameIn"));
            passwordIn.setPromptText(Dictionary.get("passwordIn"));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onCreateButtonClick() {
        try {
            List<User> users = Database.getUsersDB();

            for (User u: users) {
                if (u.username().equals(usernameIn.getText())) {
                    throw new UsernameAlreadyExistsException(Dictionary.get("UsernameAlreadyExistsException").replace("#", usernameIn.getText()));
                }
            }

            Process p = Runtime.getRuntime().exec("java ..\\CheckPassword\\src\\main\\java\\org\\example\\Main.java "  + passwordIn.getText());

            if (p.waitFor() != 0) {
                throw new PasswordTooShortException(Dictionary.get("PasswordTooShortException"));
            }

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md5.digest((passwordIn.getText()).getBytes(StandardCharsets.UTF_8));
            String md5HashedPassword = Base64.getEncoder().encodeToString(md5Hash);

            Random ran = new Random();
            String x = String.valueOf(ran.nextInt(10));

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = sha256.digest((md5HashedPassword + passwordIn.getText() + x).getBytes(StandardCharsets.UTF_8));
            String hashedPassword = Base64.getEncoder().encodeToString(sha256Hash);

            User user = new User(usernameIn.getText(), hashedPassword, "User");
            Database.insertNewUserToDatabase(user);

            AlertLib alertLib = Native.loadLibrary("AlertLib", AlertLib.class);

            alertLib.userCreatedAlert();

            LoginScreenController.newUsername = usernameIn.getText();

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            } catch (RuntimeException | IOException ex) {
                logger.error("Couldn't load new scene", ex);
            }

            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();

        } catch (IOException ex) {
            logger.error("Couldn't create new file", ex);
        } catch (UsernameAlreadyExistsException | PasswordTooShortException ex) {
            logger.info(ex.getMessage(), ex);
            message.setText(ex.getMessage());
        } catch (SQLException ex) {
            logger.error("Error comunication to database", ex);
        } catch (InterruptedException | ParserConfigurationException | SAXException | NoSuchAlgorithmException |
                 NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onCancelButtonClick() {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }
}