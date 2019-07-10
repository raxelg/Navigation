package com.rtg.navigationwtd;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

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

}
