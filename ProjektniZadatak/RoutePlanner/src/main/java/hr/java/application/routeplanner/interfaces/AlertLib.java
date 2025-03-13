package hr.java.application.routeplanner.interfaces;

import com.sun.jna.Library;

public interface AlertLib extends Library {
    void userCreatedAlert();
    int saveRideAlert();
}
