package com.rtg.navigationwtd;

import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;

public class Orientation {

    public static String which_direction(int azimuth){
        String direction = "NW";
        if (azimuth >= 350 || azimuth <= 10)
            direction = "N";
        if (azimuth < 350 && azimuth > 280)
            direction = "NW";
        if (azimuth <= 280 && azimuth > 260)
            direction = "W";
        if (azimuth <= 260 && azimuth > 190)
            direction = "SW";
        if (azimuth <= 190 && azimuth > 170)
            direction = "S";
        if (azimuth <= 170 && azimuth > 100)
            direction = "SE";
        if (azimuth <= 100 && azimuth > 80)
            direction = "E";
        if (azimuth <= 80 && azimuth > 10)
            direction = "NE";
        return direction;
    }

    public static GeoPoint nearest_node(GeoPoint current_loc, ArrayList<GeoPoint> nodes) {
            GeoPoint closest_node = nodes.get(0);
            for (int i = 0; i < nodes.size(); i++) {
                if (current_loc.distanceToAsDouble(nodes.get(i)) < current_loc.distanceToAsDouble(closest_node)) {
                    closest_node = nodes.get(i);
                }
            }
            return closest_node;
    }

    public static String doubleOrientationDirection(String userOrientation, String bearingDirection) {
        HashMap<String, String> oppositeDirections = new HashMap<String, String>(); //each cardinal direction points to it's opposite direction
        oppositeDirections.put("N", "S");
        oppositeDirections.put("S", "N");
        oppositeDirections.put("E", "W");
        oppositeDirections.put("W", "E");
        oppositeDirections.put("NW", "SE");
        oppositeDirections.put("SE", "NW");
        oppositeDirections.put("NE", "SW");
        oppositeDirections.put("SW", "NE");

        String direction = "";

        if (userOrientation == bearingDirection) {
            direction = "N";
        } else if (userOrientation == oppositeDirections.get(userOrientation)){
            direction = "S";
        } else if (userOrientation == "N" || userOrientation == "S"){
            direction = bearingDirection;
        } else if (userOrientation == "E") {
            if (bearingDirection == "N") {
                direction = "W";
            } else if (bearingDirection == "NE") {
                direction = "NW";
            } else if (bearingDirection == "NW") {
                direction = "SW";
            } else {
                direction = oppositeDirections.get(doubleOrientationDirection(userOrientation, oppositeDirections.get(bearingDirection)));
            }
        } else if (userOrientation == "SW") {
            if (bearingDirection == "N") {
                direction = "SE";
            } else if (bearingDirection == "E") {
                direction = "SW";
            } else if (bearingDirection == "NW") {
                direction = "E";
            } else {
                direction = oppositeDirections.get(doubleOrientationDirection(userOrientation, oppositeDirections.get(bearingDirection)));
            }
        } else if (userOrientation == "NW") {
            if (bearingDirection == "N") {
                direction = "NE";
            } else if (bearingDirection == "E") {
                direction = "SE";
            } else if (bearingDirection == "SW") {
                direction = "W";
            } else {
                direction = oppositeDirections.get(doubleOrientationDirection(userOrientation, oppositeDirections.get(bearingDirection)));
            }
        } else {
            direction = oppositeDirections.get(doubleOrientationDirection(oppositeDirections.get(userOrientation), bearingDirection));
        }
        return direction;
    }
}
