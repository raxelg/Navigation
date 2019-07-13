package com.rtg.navigationwtd;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.rtg.MyApp;

import java.util.List;

public class GeoCoder {

    public static String getLocationFromAddress(String strAddress) {

        Context mContext = MyApp.getContext();
        Geocoder coder = new Geocoder(mContext);
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            return lat + "," + lng;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAddressFromCoords(double lat, double lon) {

        Context mContext = MyApp.getContext();
        Geocoder coder = new Geocoder(mContext);
        List<Address> address;

        try {
            address = coder.getFromLocation(lat,lon,1);
            if (address == null) {
                return null;
            }
            String addressLine = address.get(0).getAddressLine(0);
            return addressLine;

        } catch (Exception e) {
            return null;
        }
    }


}
