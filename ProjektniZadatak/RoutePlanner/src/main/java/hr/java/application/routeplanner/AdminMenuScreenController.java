package hr.java.application.routeplanner;

import com.github.vincentrussell.ini.Ini;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import hr.java.application.routeplanner.entity.*;
import hr.java.application.routeplanner.entity.Dictionary;
import hr.java.application.routeplanner.records.User;
import hr.java.application.routeplanner.thread.InsertToReport;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.fop.apps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static hr.java.application.routeplanner.HelloApplication.mainStage;

public class AdminMenuScreenController implements Initializable{
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    User user;
    private List<Label> list = new ArrayList<>();

    @FXML
    private ImageView logo;
    @FXML
    private ImageView backButton;
    @FXML
    private ImageView importButton;
    @FXML
    private VBox vb;
    @FXML
    private ComboBox lang;
    @FXML
    private ComboBox unit;

    @FXML
    private Label routesLabel;
    @FXML
    private Label ridesLabel;
    @FXML
    private Label logLabel;
    @FXML
    private Label adminLabel;
    @FXML
    private Label lngLabel;
    @FXML
    private Label unitLabel;
    @FXML
    private Label reportLabel;
    @FXML
    private Label logOutLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logo.setImage(new Image(new FileInputStream("img/logo.png")));
            backButton.setImage(new Image(new FileInputStream("img/back.png")));
            importButton.setImage(new Image(new FileInputStream("img/import.png")));
        }
        catch (FileNotFoundException ex){
            logger.error("Image file not found", ex);
        }

        user = LoginScreenController.user;

        try (FileInputStream ulazIni = new FileInputStream("dat/settings.ini")){
            Ini ini = new Ini();
            ini.load(ulazIni);

            lang.getItems().addAll("EN", "HR", "IT", "FR", "PL");
            String langIni = (String) ini.getValue("Language", "lang");
            lang.getSelectionModel().select(langIni);

            unit.getItems().addAll("Metric", "Imperial");
            String unitIni = (String) ini.getValue("Units", "unit");
            unit.getSelectionModel().select(unitIni);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            routesLabel.setText(Dictionary.get("RoutesLabel"));
            ridesLabel.setText(Dictionary.get("RidesLabel"));
            logLabel.setText(Dictionary.get("LogLabel"));
            adminLabel.setText(Dictionary.get("AdminLabel"));
            lngLabel.setText(Dictionary.get("LngLabel"));
            unitLabel.setText(Dictionary.get("UnitLabel"));
            reportLabel.setText(Dictionary.get("ReportLabel"));
            logOutLabel.setText(Dictionary.get("LogOutLabel"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBackButtonPress() {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainScreen.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    @FXML
    private void onRoutesPres() {
        for (Label l: list) {
            vb.getChildren().remove(l);
        }
        list.clear();
        //List<Route> routes = user.getRoutes();
        try {
            List<Route> routes = Database.getRoutesByUsernameDB(user.username());
            for(int i = 0; i < routes.size(); i++) {
                Label route = new Label((String) routes.get(i).getName());
                vb.getChildren().add(1 + i, route);
                route.setStyle("-fx-font-size:22px");
                VBox.setMargin(route, new Insets(0, 0, 0, 30));

                ContextMenu cm = new ContextMenu();
                MenuItem export = new MenuItem("Export");
                export.setOnAction(event -> {
                    cm.hide();
                    for (Route r: routes) {
                        if (r.getName().equals(route.getText())){
                            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("dat/tmp.dat"))) {
                                out.writeObject(r);
                            } catch (IOException ex) {
                                logger.error("Error exporting route data", ex);
                            }
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Save");
                            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".dat", "*.dat*"));
                            File dest = fileChooser.showSaveDialog(mainStage);
                            if (dest != null) {
                                try {
                                    Files.copy(new File("dat/tmp.dat").toPath(), dest.toPath());
                                } catch (IOException ex) {
                                    logger.error("Error exporting route data", ex);
                                }
                            }
                        }
                    }
                });
                cm.getItems().add(export);
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction(event -> {
                    cm.hide();
                    for (Route r: routes) {
                        if (r.getName().equals(route.getText())){
                            try {
                                Database.removeRoute(r);
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
                                ChangeLog.log(new Log(dtf.format(now), "Removed a route", "Removed route " + r.getName(), user.username(), user.role()));
                            } catch (SQLException | IOException ex) {
                                logger.error("Error communicating to database", ex);
                            }
                            for (Label l: list) {
                                if (l.getText().equals(r.getName())){
                                    vb.getChildren().remove(l);
                                    list.remove(l);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                });
                cm.getItems().add(delete);

                route.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent t) {
                        if(t.getButton() == MouseButton.PRIMARY) {
                            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainScreen.fxml"));
                            Scene scene = null;
                            try {
                                scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
                                HelloApplication.getStage().setScene(scene);
                                HelloApplication.getStage().show();
                            } catch (IOException ex) {
                                logger.error("Couldn't load new scene", ex);
                            }
                            MainScreenController controller = fxmlLoader.getController();
                            controller.drawRoute(route.getText());
                        }
                        if(t.getButton() == MouseButton.SECONDARY) {
                            cm.show(route, t.getScreenX(), t.getScreenY());
                        }
                    }
                });
                list.add(route);
            }
        } catch (SQLException | IOException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onRidesPres() {
        for (Label l: list) {
            vb.getChildren().remove(l);
        }
        list.clear();
        List<Ride> rides = new ArrayList<>();
        try {
            rides = Database.getRidesDB();
        } catch (SQLException | IOException ex) {
            logger.error("Error reading from database", ex);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        int j = 0;
        for(int i = 0; i < rides.size(); i++) {
            if (Objects.equals(rides.get(i).getUsername(), user.username())){
                Label ride = new Label((String) rides.get(i).getName());
                vb.getChildren().add(3 + j, ride);
                ride.setStyle("-fx-font-size:22px");
                VBox.setMargin(ride, new Insets(0, 0, 0, 30));
                ride.setOnMouseClicked(mouseEvent -> {
                    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("rideScreen.fxml"));
                    Scene scene = null;
                    try {
                        scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
                        HelloApplication.getStage().setScene(scene);
                        HelloApplication.getStage().show();
                    } catch (IOException ex) {
                        logger.error("Couldn't load new scene", ex);
                    }
                    RideScreenController.drawRide(ride.getText());
                });
                list.add(ride);
                j++;
            }
        }
    }

    @FXML
    private void onChangeLogPres() {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("changeLogScreen.fxml"));
            Scene scene = null;
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    @FXML
    private void onAdminScreenPres() {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("adminScreen.fxml"));
            Scene scene = null;
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    @FXML
    private void onLogOutPres() {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
            Scene scene = null;
            scene = new Scene(fxmlLoader.load(), HelloApplication.getStage().getScene().getWidth(), HelloApplication.getStage().getScene().getHeight());
            HelloApplication.getStage().setScene(scene);
            HelloApplication.getStage().show();
        } catch (RuntimeException | IOException ex) {
            logger.error("Couldn't load new scene", ex);
        }
    }

    @FXML
    private void onImportPres() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open route file");
        File dest = fileChooser.showOpenDialog(mainStage);

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(dest.getAbsolutePath()))){
            Database.insertNewRouteToDatabase((Route) in.readObject());
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Error importing route", ex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        onRoutesPres();
    }

    @FXML
    private void onSettingsChange() {
        try (FileWriter izlazIni = new FileWriter("dat/settings.ini")){
            String langIni = (String) lang.getSelectionModel().getSelectedItem();
            String unitIni = (String) unit.getSelectionModel().getSelectedItem();

            Ini ini = new Ini();
            ini.putValue("Language", "lang", langIni);
            ini.putValue("Units", "unit", unitIni);
            ini.store(izlazIni, "");

            MainScreenController.unitType = unitIni;

            routesLabel.setText(Dictionary.get("RoutesLabel"));
            ridesLabel.setText(Dictionary.get("RidesLabel"));
            logLabel.setText(Dictionary.get("LogLabel"));
            adminLabel.setText(Dictionary.get("AdminLabel"));
            lngLabel.setText(Dictionary.get("LngLabel"));
            unitLabel.setText(Dictionary.get("UnitLabel"));
            reportLabel.setText(Dictionary.get("ReportLabel"));
            logOutLabel.setText(Dictionary.get("LogOutLabel"));

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onGenerateReportPres() {
        try {
            List<Ride> rideList = Database.getRidesByUsernameDB(user.username());

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("dat/report.pdf"));
            document.open();

            document.addAuthor(user.username());
            document.addCreationDate();
            document.addCreator("Route Planner");
            document.addTitle("User ride data report");
            document.addSubject("Report of all user rides.");

            com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("img/logoBlack.png");
            logo.scaleAbsolute(150, 70);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            Paragraph title = new Paragraph(new Phrase("Ride report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30f)));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);

            Paragraph userTitle = new Paragraph(new Phrase("User data", FontFactory.getFont(FontFactory.HELVETICA, 15f)));
            userTitle.setSpacingBefore(20f);
            Paragraph username = new Paragraph("Username: " + user.username());
            Paragraph role = new Paragraph("Role: " + user.role());
            double totalTime = rideList.stream().map(ride -> ride.getDistanceTraveled()/ride.getAvgSpeed()).reduce((double) 0, Double::sum);
            System.out.println(rideList);
            Paragraph time = new Paragraph("Total time spent riding: " + String.format("%02d:%02d:%02d", (int)(totalTime), (int)(totalTime*60%60), Math.round(totalTime*60*60%60)));
            Paragraph distance = new Paragraph("Total distance traveled: " + Math.round(rideList.stream().map(Ride::getDistanceTraveled).reduce((double) 0, Double::sum)*100)/100f);

            Paragraph tableTitle = new Paragraph(new Phrase("Ride data", FontFactory.getFont(FontFactory.HELVETICA, 15f)));
            tableTitle.setSpacingBefore(20f);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {1.5f, 1f, 1f, 1f, 1f};
            table.setWidths(columnWidths);

            PdfPCell nameCell = new PdfPCell(new Paragraph("Name"));
            nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            nameCell.setBorderColor(BaseColor.BLACK);
            nameCell.setPaddingLeft(5);
            nameCell.setPaddingBottom(5);

            PdfPCell distanceCell = new PdfPCell(new Paragraph("Distance"));
            distanceCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            distanceCell.setBorderColor(BaseColor.BLACK);
            distanceCell.setPaddingLeft(5);
            distanceCell.setPaddingBottom(5);

            PdfPCell timeCell = new PdfPCell(new Paragraph("Time"));
            timeCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            timeCell.setBorderColor(BaseColor.BLACK);
            timeCell.setPaddingLeft(5);
            timeCell.setPaddingBottom(5);

            PdfPCell avgSpeedCell = new PdfPCell(new Paragraph("Average speed"));
            avgSpeedCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            avgSpeedCell.setBorderColor(BaseColor.BLACK);
            avgSpeedCell.setPaddingLeft(5);
            avgSpeedCell.setPaddingBottom(5);

            PdfPCell maxSpeedCell = new PdfPCell(new Paragraph("Max speed"));
            maxSpeedCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            maxSpeedCell.setBorderColor(BaseColor.BLACK);
            maxSpeedCell.setPaddingLeft(5);
            maxSpeedCell.setPaddingBottom(5);

            table.addCell(nameCell);
            table.addCell(distanceCell);
            table.addCell(timeCell);
            table.addCell(avgSpeedCell);
            table.addCell(maxSpeedCell);

            ExecutorService executorService = Executors.newCachedThreadPool();
            for (Ride r: rideList) {
                executorService.execute(new InsertToReport(r, table));
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }

            document.add(title);
            document.add(userTitle);
            document.add(username);
            document.add(role);
            document.add(time);
            document.add(distance);
            document.add(tableTitle);
            document.add(table);

            document.close();

            java.awt.Desktop.getDesktop().open(new File("dat/report.pdf"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}