module hr.java.application.routeplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.maps;
    requires graphhopper;
    requires okhttp;
    requires org.json;
    requires org.slf4j;
    requires java.sql;
    requires java.prefs;
    requires java.ini.parser;
    requires fop.core;
    requires itextpdf;
    requires java.desktop;
    requires com.sun.jna;
    requires java.sql.rowset;


    exports hr.java.application.routeplanner;
    opens hr.java.application.routeplanner to javafx.fxml;
}