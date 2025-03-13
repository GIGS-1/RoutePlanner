package hr.java.application.routeplanner.interfaces;

import hr.java.application.routeplanner.AdminScreenController;

public sealed interface EntityScreens permits AdminScreenController {
    void drawRouteScreen();
    void drawRideScreen();
    void drawUserScreen();
}
