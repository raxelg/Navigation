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
        HashMap<String, String> oppositeDirections = new HashMap<String, String>();
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
            direction = userOrientation;
        } else if (userOrientation == "N" || userOrientation == "E" || userOrientation == "S" || userOrientation == "W") {
            if (bearingDirection == oppositeDirections.get(userOrientation)) {
                direction = bearingDirection;
            } else if (bearingDirection.contains(userOrientation)) {
                direction = bearingDirection.replace(userOrientation, "");
            } else if (bearingDirection.contains(oppositeDirections.get(userOrientation)) && bearingDirection.length() > 1) {
                direction = bearingDirection;
            } else {
                if (bearingDirection == "E" || bearingDirection == "W") {
                    direction = userOrientation + bearingDirection;
                }
                if (bearingDirection == "N" || bearingDirection == "S") {
                    direction = bearingDirection + userOrientation;
                    }
                }

        } else if(userOrientation == "NW" || userOrientation == "SE" || userOrientation == "NE" || userOrientation == "SW") {
                if (bearingDirection == oppositeDirections.get(userOrientation) || bearingDirection.charAt(0) == oppositeDirections.get(userOrientation).charAt(0) || bearingDirection.charAt(0) == oppositeDirections.get(userOrientation).charAt(1)) {
                    direction = oppositeDirections.get(userOrientation);
                } else if (userOrientation.charAt(0) == bearingDirection.charAt(0)) {
                    direction = userOrientation.charAt(0) + oppositeDirections.get(userOrientation.charAt(1));
                } else if (userOrientation.charAt(0) == bearingDirection.charAt(0) && userOrientation.charAt(1) == oppositeDirections.get(bearingDirection.charAt(1)).charAt(0)) {
                    direction = oppositeDirections.get(userOrientation.charAt(1));
                } else if (userOrientation.charAt(1) == bearingDirection.charAt(0)) {
                    direction = oppositeDirections.get(userOrientation.charAt(0)) + userOrientation.charAt(1);
                } else if (userOrientation.charAt(1) == bearingDirection.charAt(1) && userOrientation.charAt(0) == oppositeDirections.get(bearingDirection.charAt(0)).charAt(0)) {
                    direction = oppositeDirections.get(userOrientation.charAt(0));
                }
        } return direction;
    }
}
