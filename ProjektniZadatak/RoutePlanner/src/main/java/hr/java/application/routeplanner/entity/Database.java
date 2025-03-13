package hr.java.application.routeplanner.entity;

import com.gluonhq.maps.MapPoint;
import hr.java.application.routeplanner.exceptions.FileHeaderNotMatching;
import hr.java.application.routeplanner.records.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;

public class Database {

    //Connection
    private static final String DATABASE_FILE = "dat/database.properties";

    public static Connection connectToDatabase() throws SQLException, IOException {
        Properties svojstva = new Properties();
        svojstva.load(new FileReader(DATABASE_FILE));
        String urlBazePodataka = svojstva.getProperty("bazaPodatakaUrl");
        String korisnickoIme = svojstva.getProperty("korisnickoIme");
        String lozinka = svojstva.getProperty("lozinka");
        Connection veza = DriverManager.getConnection(urlBazePodataka, korisnickoIme,lozinka);
        return veza;
    }

    public static void disconnectFromDatabase(Connection connection) throws SQLException {
        connection.close();
    }

    public static List<Ride> getRidesDB() throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        List<Ride> rides = new ArrayList<>();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT * FROM RIDE");
        while(rs.next()) {
            String newName = rs.getString("NAME");
            Blob newRidePointsBlob = rs.getBlob("RIDE_POINTS");
            Double newDistanceTraveled = rs.getDouble("DISTANCE_TRAVELED");
            Double newSpeed = rs.getDouble("SPEED");
            Double newAvgSpeed = rs.getDouble("AVG_SPEED");
            Double newMaxSpeed = rs.getDouble("MAX_SPEED");
            int newUserId = rs.getInt("USER_ID");

            FileOutputStream outputStream = new FileOutputStream("dat/encoded.enc");
            outputStream.write(newRidePointsBlob.getBytes(1, (int) newRidePointsBlob.length()));
            outputStream.close();

            AES.decrypt("dat/encoded.enc", "dat/decoded.json");

            FileInputStream inputStream = new FileInputStream("dat/decoded.json");
            String string = new String(inputStream.readAllBytes());
            inputStream.close();

            JSONObject jsonObject = new JSONObject(string.substring(string.indexOf("{")));

            String newRidePoints = "";

            for (Object point: jsonObject.getJSONArray("coordinates")) {
                JSONObject pointJSON = (JSONObject) point;
                newRidePoints += ";" + pointJSON.get("lat") + "," + pointJSON.get("lng");
            }

            newRidePoints = newRidePoints.substring(1);

            rides.add(new Ride(newName, newRidePoints, newDistanceTraveled, newSpeed, newAvgSpeed, newMaxSpeed, getUserByID(newUserId)));
        }

        File file1 = new File("dat/decoded.json");
        file1.delete();

        disconnectFromDatabase(connection);
        return rides;
    }

    public static List<Ride> getRidesByUsernameDB(String username) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        List<Ride> rides = new ArrayList<>();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT * FROM RIDE WHERE USER_ID='" + getUserByUsername(username) + "'");
        while(rs.next()) {
            String newName = rs.getString("NAME");
            Blob newRidePointsBlob = rs.getBlob("RIDE_POINTS");
            Double newDistanceTraveled = rs.getDouble("DISTANCE_TRAVELED");
            Double newSpeed = rs.getDouble("SPEED");
            Double newAvgSpeed = rs.getDouble("AVG_SPEED");
            Double newMaxSpeed = rs.getDouble("MAX_SPEED");
            int newUserId = rs.getInt("USER_ID");

            FileOutputStream outputStream = new FileOutputStream("dat/encoded.enc");
            outputStream.write(newRidePointsBlob.getBytes(1, (int) newRidePointsBlob.length()));
            outputStream.close();

            AES.decrypt("dat/encoded.enc", "dat/decoded.json");

            FileInputStream inputStream = new FileInputStream("dat/decoded.json");
            String string = new String(inputStream.readAllBytes());
            inputStream.close();

            JSONObject jsonObject = new JSONObject(string.substring(string.indexOf("{")));

            String newRidePoints = "";

            for (Object point: jsonObject.getJSONArray("coordinates")) {
                JSONObject pointJSON = (JSONObject) point;
                newRidePoints += ";" + pointJSON.get("lat") + "," + pointJSON.get("lng");
            }

            newRidePoints = newRidePoints.substring(1);

            rides.add(new Ride(newName, newRidePoints, newDistanceTraveled, newSpeed, newAvgSpeed, newMaxSpeed, getUserByID(newUserId)));
        }

        File file1 = new File("dat/decoded.json");
        file1.delete();

        disconnectFromDatabase(connection);
        return rides;
    }

    public static void insertNewRideToDatabase(Ride ride) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        JSONArray coordinates = new JSONArray();
        for (MapPoint point: ride.getRidePoints()) {
            coordinates.put(new JSONObject().put("lat", point.getLatitude()).put("lng", point.getLongitude()));
        }
        JSONObject jsonObject = new JSONObject()
                .put("coordinates", coordinates);

        try (ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream("dat/tmp.json"))) {
            objectOut.writeObject(jsonObject.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        AES.encrypt("dat/tmp.json", "dat/encoded.enc");

        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO RIDE (NAME, RIDE_POINTS, DISTANCE_TRAVELED, SPEED, AVG_SPEED, MAX_SPEED, USER_ID) VALUES(?, ?, ?, ?, ?, ?, ?)");
        stmt.setString(1, (String) ride.getName());
        stmt.setBlob(2, new FileInputStream("dat/encoded.enc"));
        stmt.setDouble(3, ride.getDistanceTraveled());
        stmt.setDouble(4, ride.getSpeed());
        stmt.setDouble(5, ride.getAvgSpeed());
        stmt.setDouble(6, ride.getMaxSpeed());
        stmt.setInt(7, getUserByUsername(ride.getUsername()));
        stmt.executeUpdate();
        disconnectFromDatabase(connection);

        File file1 = new File("dat/encoded.enc");
        file1.delete();
    }

    public static void editRide(Ride ride, Ride newRide) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT RIDE_POINTS FROM RIDE WHERE NAME='" + ride.getName() + "'");
        while(rs.next()) {
            Blob newRidePointsBlob = rs.getBlob("RIDE_POINTS");

            FileOutputStream outputStream = new FileOutputStream("dat/encoded.enc");
            outputStream.write(newRidePointsBlob.getBytes(1, (int) newRidePointsBlob.length()));
            outputStream.close();

            AES.decrypt("dat/encoded.enc", "dat/decoded.json");

            FileInputStream inputStream = new FileInputStream("dat/decoded.json");
            String string = new String(inputStream.readAllBytes());
            inputStream.close();

            JSONObject jsonObject = new JSONObject(string.substring(string.indexOf("{")));

            jsonObject.getJSONArray("coordinates").clear();

            for (MapPoint point: newRide.getRidePoints()) {
                jsonObject.getJSONArray("coordinates").put(new JSONObject().put("lat", point.getLatitude()).put("lng", point.getLongitude()));
            }

            try (ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream("dat/tmp.json"))) {
                objectOut.writeObject(jsonObject.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        AES.encrypt("dat/tmp.json", "dat/encoded.enc");

        PreparedStatement stmt = connection.prepareStatement("UPDATE RIDE SET NAME=?, RIDE_POINTS=?, DISTANCE_TRAVELED=?, SPEED=?, AVG_SPEED=?, MAX_SPEED=?, USER_ID=? WHERE NAME='" + ride.getName() + "'");
        stmt.setString(1, (String) newRide.getName());
        stmt.setBlob(2, new FileInputStream("dat/encoded.enc"));
        stmt.setDouble(3, newRide.getDistanceTraveled());
        stmt.setDouble(4, newRide.getSpeed());
        stmt.setDouble(5, newRide.getAvgSpeed());
        stmt.setDouble(6, newRide.getMaxSpeed());
        stmt.setInt(7, getUserByUsername(newRide.getUsername()));
        stmt.executeUpdate();

        disconnectFromDatabase(connection);

        File file1 = new File("dat/encoded.enc");
        file1.delete();
        File file2 = new File("dat/decoded.json");
        file2.delete();
    }

    public static void removeRide(Ride ride) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM RIDE WHERE NAME = ?");
        stmt.setString(1, (String) ride.getName());
        stmt.executeUpdate();
        disconnectFromDatabase(connection);
    }

    public static List<Route> getRoutesDB() throws SQLException, IOException {
        try {
            Connection connection = connectToDatabase();
            List<Route> routes = new ArrayList<>();
            Statement sqlStatement = connection.createStatement();
            ResultSet rs = sqlStatement.executeQuery("SELECT * FROM ROUTE");
            while(rs.next()) {
                String newName = rs.getString("NAME");
                Blob newWaypoints = rs.getBlob("WAYPOINTS");
                int newUserId = rs.getInt("USER_ID");

                StreamSource a =  new StreamSource(newWaypoints.getBinaryStream());
                StreamResult result = new StreamResult("dat/tmp.xml");
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.transform(a,result);

                List<Waypoint> waypoints = new ArrayList<>();

                File fXmlFile = new File("dat/tmp.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("point");

                for(int i = 0; i<nList.getLength(); i++){
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                        String lat = eElement.getElementsByTagName("lat").item(0).getTextContent();
                        String lng = eElement.getElementsByTagName("lng").item(0).getTextContent();
                        waypoints.add(new Waypoint(name, Double.parseDouble(lat), Double.parseDouble(lng)));
                    }
                }

                routes.add(new Route.RouteBuilder().name(newName).waypoints(waypoints).userID(newUserId).build());

                File waypointsFile = new File("dat/tmp.xml");
                waypointsFile.delete();
            }

            disconnectFromDatabase(connection);

            return routes;
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Route> getRoutesByUsernameDB(String username) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        try {
            Connection connection = connectToDatabase();
            List<Route> routes = new ArrayList<>();
            Statement sqlStatement = connection.createStatement();
            ResultSet rs = sqlStatement.executeQuery("SELECT * FROM ROUTE WHERE USER_ID='" + getUserByUsername(username) + "'");
            while(rs.next()) {
                String newName = rs.getString("NAME");
                Blob newWaypoints = rs.getBlob("WAYPOINTS");
                int newUserId = rs.getInt("USER_ID");

                StreamSource a =  new StreamSource(newWaypoints.getBinaryStream());
                StreamResult result = new StreamResult("dat/tmp.xml");
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.transform(a,result);

                List<Waypoint> waypoints = new ArrayList<>();

                File fXmlFile = new File("dat/tmp.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("point");

                for(int i = 0; i<nList.getLength(); i++){
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                        String lat = eElement.getElementsByTagName("lat").item(0).getTextContent();
                        String lng = eElement.getElementsByTagName("lng").item(0).getTextContent();
                        waypoints.add(new Waypoint(name, Double.parseDouble(lat), Double.parseDouble(lng)));
                    }
                }

                routes.add(new Route.RouteBuilder().name(newName).waypoints(waypoints).userID(newUserId).build());

                File waypointsFile = new File("dat/tmp.xml");
                waypointsFile.delete();
            }

            disconnectFromDatabase(connection);

            return routes;
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertNewRouteToDatabase(Route route) throws SQLException, IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element waypoints = doc.createElement("waypoints");
            doc.appendChild(waypoints);

            for (Waypoint w: route.getWaypoints()) {
                Element point = doc.createElement("point");
                waypoints.appendChild(point);

                Element name = doc.createElement("name");
                name.setTextContent(w.getName().toString());
                point.appendChild(name);
                Element lat = doc.createElement("lat");
                lat.setTextContent(w.getLat().toString());
                point.appendChild(lat);
                Element lng = doc.createElement("lng");
                lng.setTextContent(w.getLng().toString());
                point.appendChild(lng);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult("dat/tmp.xml");
            transformer.transform(source, result);

            Connection connection = connectToDatabase();
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO ROUTE (NAME, WAYPOINTS, USER_ID) VALUES(?, ?, ?)");
            stmt.setString(1, (String) route.getName());
            stmt.setBlob(2, new FileInputStream("dat/tmp.xml"));
            stmt.setInt(3, route.getUserID());
            stmt.executeUpdate();
            disconnectFromDatabase(connection);

            File waypointsFile = new File("dat/tmp.xml");
            waypointsFile.delete();

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static void editRoute(Route route, Route newRoute) throws SQLException, IOException {
        try {
            Connection connection = connectToDatabase();
            Statement sqlStatement = connection.createStatement();
            ResultSet rs = sqlStatement.executeQuery("SELECT WAYPOINTS FROM ROUTE WHERE NAME='" + route.getName() + "'");
            while(rs.next()) {
                Blob newWaypoints = rs.getBlob("WAYPOINTS");

                StreamSource streamSource =  new StreamSource(newWaypoints.getBinaryStream());
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(streamSource.getInputStream());
                doc.getDocumentElement().normalize();

                NodeList points = doc.getElementsByTagName("point");
                int len = points.getLength();
                for(int i = 0; i < len; i++){
                    Element point = (Element) points.item(0);
                    point.getParentNode().removeChild(point);
                }

                for (Waypoint w: newRoute.getWaypoints()) {
                    Element point = doc.createElement("point");
                    doc.getElementsByTagName("waypoints").item(0).appendChild(point);

                    Element lat = doc.createElement("lat");
                    lat.setTextContent(w.getLat().toString());
                    point.appendChild(lat);
                    Element lng = doc.createElement("lng");
                    lng.setTextContent(w.getLng().toString());
                    point.appendChild(lng);
                }

                StreamResult result = new StreamResult("dat/tmp.xml");
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);
            }

            PreparedStatement stmt = connection.prepareStatement("UPDATE ROUTE SET NAME=?, WAYPOINTS=? WHERE NAME='" + route.getName() + "'");
            stmt.setString(1, (String) newRoute.getName());
            stmt.setBlob(2, new FileInputStream("dat/tmp.xml"));
            stmt.executeUpdate();
            disconnectFromDatabase(connection);

            File waypointsFile = new File("dat/tmp.xml");
            waypointsFile.delete();

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeRoute(Route route) throws SQLException, IOException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM ROUTE WHERE NAME = ?");
        stmt.setString(1, (String) route.getName());
        stmt.executeUpdate();
        disconnectFromDatabase(connection);
    }

    public static List<User> getUsersDB() throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        List<User> users = new ArrayList<>();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT * FROM USER_TABLE");
        while(rs.next()) {
            String newUsername = RSA.decrypt(rs.getBlob("USERNAME"));
            String newPassword = rs.getString("PASSWORD");
            String newRole = rs.getString("ROLE");
            users.add(new User(newUsername, newPassword, newRole));
        }
        disconnectFromDatabase(connection);
        return users;
    }

    public static String getUserByID(int id) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT USERNAME FROM USER_TABLE WHERE ID='" + id + "'");
        String newUsername = null;
        while(rs.next()) {
            newUsername = RSA.decrypt(rs.getBlob("USERNAME"));
        }
        disconnectFromDatabase(connection);
        return newUsername;
    }

    public static int getUserByUsername(String username) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        Statement sqlStatement = connection.createStatement();
        ResultSet rs = sqlStatement.executeQuery("SELECT * FROM USER_TABLE");
        while(rs.next()) {
            String newUsername = RSA.decrypt(rs.getBlob("USERNAME"));
            if (newUsername.equals(username)){
                return rs.getInt("ID");
            }
        }
        disconnectFromDatabase(connection);

        return -1;
    }

    public static void insertNewUserToDatabase(User user) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO USER_TABLE (USERNAME, PASSWORD, ROLE) VALUES(?, ?, ?)");
        stmt.setBlob(1, RSA.encrypt(user.username()));
        stmt.setString(2, user.password());
        stmt.setString(3, user.role());
        stmt.executeUpdate();
        disconnectFromDatabase(connection);
    }

    public static void removeUser(User user) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        int id = getUserByUsername(user.username());

        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM USER_TABLE WHERE ID = ?");
        stmt.setInt(1, id);
        stmt.executeUpdate();
        disconnectFromDatabase(connection);
    }

    public static void editUser(User user, User newUser) throws SQLException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        int id = getUserByUsername(user.username());

        Connection connection = connectToDatabase();
        PreparedStatement stmt = connection.prepareStatement("UPDATE USER_TABLE SET USERNAME=?, PASSWORD=?, ROLE=? WHERE ID='" + id + "'");
        stmt.setBlob(1, RSA.encrypt(newUser.username()));
        stmt.setString(2, newUser.password());
        stmt.setString(3, newUser.role());
        stmt.executeUpdate();
        disconnectFromDatabase(connection);
    }
}

