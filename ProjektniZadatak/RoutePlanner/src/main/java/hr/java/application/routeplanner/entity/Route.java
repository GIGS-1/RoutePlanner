package hr.java.application.routeplanner.entity;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import hr.java.application.routeplanner.MainScreenController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Route extends NamedEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 6529685098267757690L;
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private List<Waypoint> routePoints;
    private List<Waypoint> waypoints;
    private int userID;

    public Route() {}

    public Route(RouteBuilder builder) {
        super(builder.name);
        this.waypoints = builder.waypoints;
        this.routePoints = builder.routePoints;
        this.userID = builder.userID;
    }

    public List<Waypoint> getRoutePoints() {
        if (routePoints == null) {
            routePoints = new ArrayList<>();
            try {
                createRoute();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return routePoints;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public int getUserID() {
        return userID;
    }

    private BufferedReader requestRoute() throws IOException {
        OkHttpClient client = new OkHttpClient();
        StringBuilder req = new StringBuilder("https://graphhopper.com/api/1/route?");
        for (Waypoint waypoint : waypoints) {
            req.append(String.format("point=%f,%f&", waypoint.getLat(), waypoint.getLng()));
        }
        req.append("profile=car&locale=en&calc_points=true&points_encoded=false&key=503acf37-651a-4784-8d7d-73925f464e20");
        Request request = new Request.Builder().url(req.toString()).get().build();

        Response response = client.newCall(request).execute();

        BufferedInputStream routeResponseBin = new BufferedInputStream(new URL(response.request().urlString()).openStream());
        return new BufferedReader(new InputStreamReader(routeResponseBin, StandardCharsets.UTF_8));
    }

    private String[] extractRoute() throws IOException {
        BufferedReader routeResponse = requestRoute();

        String[] string = routeResponse.readLine().split("coordinates");
        string = string[1].split("instructions");
        string[0] = string[0].replace("\":[", "");
        string[0] = string[0].replace("]},\"", "");
        string[0] = string[0].replace("]", "");
        string[0] = string[0].replace("[", "");
        return string[0].split(",");
    }

    private void createRoute() throws IOException {
        String[] routePointsArray = extractRoute();

        routePoints.clear();
        boolean check = true;
        Double lat = null, lon = null;
        for (String point: routePointsArray) {
            if (check) {
                lon = Double.parseDouble(point);
                check = false;
            } else {
                lat = Double.parseDouble(point);
                routePoints.add(new Waypoint(lat, lon));
                check = true;
            }
        }
    }

    public static class RouteBuilder{
        private String name;
        private List<Waypoint> waypoints;
        private List<Waypoint> routePoints;
        private int userID;
        public RouteBuilder(){}
        public  RouteBuilder name(String name){
            this.name = name;
            return this;
        }
        public  RouteBuilder waypoints(List<Waypoint> waypoints){
            this.waypoints = waypoints;
            return this;
        }
        public  RouteBuilder userID(int userID){
            this.userID = userID;
            return this;
        }
        public  RouteBuilder routePoints(List<Waypoint> routePoints){
            this.routePoints = routePoints;
            return this;
        }
        public Route build(){
            return new Route(this);
        }
    }
}