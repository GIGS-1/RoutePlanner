package hr.java.application.routeplanner.entity;

import hr.java.application.routeplanner.MainScreenController;
import hr.java.application.routeplanner.exceptions.FieldIsEmptyException;
import hr.java.application.routeplanner.exceptions.FileHeaderNotMatching;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChangeLog {
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private static List<Log> logs = new ArrayList<>();

    public static void log(Log log) {
        logs = readLogs();
        logs.add(log);

        try (ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream("dat/changeLog.dat"))) {
            objectOut.writeObject(new LogHeader());
            objectOut.writeObject(logs);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public static List<Log> readLogs() {
        try (ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream("dat/changeLog.dat"))){
            LogHeader logHeader = (LogHeader) objectIn.readObject();
            if (!logHeader.getName().equals("LogHeader") || logHeader.getVersion() != 1.0){
                throw new FileHeaderNotMatching("The header does not match the object type.");
            }
            logs = (List<Log>) objectIn.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (FileHeaderNotMatching ex) {
            logger.error("Canot read logs | Header does not match.", ex);
        }
        return logs;
    }
}
