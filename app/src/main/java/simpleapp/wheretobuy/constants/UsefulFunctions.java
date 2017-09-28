package simpleapp.wheretobuy.constants;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.model.LatLng;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class UsefulFunctions {

    public static boolean isOnline(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static int getStatusBarHeight(Context c) {
        int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return c.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getScreenHeight(Activity a){
        DisplayMetrics dm = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static String getPriceFormat(double price){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("PLN"));
        return format.format(price);
    }

    public static String getDistanceKilometersFormat(double distance){
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return  df.format(distance/1000) + "km";
    }

    public static double getDistanceBetween(LatLng from, LatLng to) {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);
        return (double) results[0];
    }

    public static int getPixelsFromDp(Context c, float sizeDp) {
        return (int) (sizeDp * c.getResources().getDisplayMetrics().density);
    }

    public static String deleteEndSpace(String input){
        if (input.length() > 0) {
            if (input.charAt(input.length() - 1) == ' ') {
                input = input.substring(0, input.length() - 1);
            }
        }
        return input;
    }

    public static String getMostCommonString(List<String> list) {
        String previous = null;
        int bestCount = 0;
        int count = 1;
        String bestString = null;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(previous)) {
                count++;
            } else {
                count = 1;
            }

            if (count > bestCount) {
                bestCount = count;
                bestString = list.get(i);
            }

            previous = list.get(i);
        }

        return bestString;
    }
}