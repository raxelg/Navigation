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
}
