package hr.java.application.routeplanner.records;

import hr.java.application.routeplanner.MainScreenController;
import hr.java.application.routeplanner.entity.Route;
import hr.java.application.routeplanner.entity.Waypoint;
import hr.java.application.routeplanner.exceptions.PasswordTooShortException;
import hr.java.application.routeplanner.exceptions.UsernameAlreadyExistsException;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public record User(String username, String password, String role) {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
}
