package simpleapp.wheretobuy.constants;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UsefulFunctions {

    public static boolean isOnline(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}