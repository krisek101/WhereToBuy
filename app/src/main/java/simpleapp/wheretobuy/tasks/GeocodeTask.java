package simpleapp.wheretobuy.tasks;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;

public class GeocodeTask extends AsyncTask<Void, Void, String> {

    private MapActivity mapActivity;
    private LatLng position;
    private TextView callbackView;

    public GeocodeTask(MapActivity mapActivity, LatLng position, TextView callbackView) {
        this.mapActivity = mapActivity;
        this.position = position;
        this.callbackView = callbackView;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String address = "";
        if (UsefulFunctions.isOnline(mapActivity)) {
            Geocoder geocoder = new Geocoder(mapActivity, Locale.getDefault());
            List<Address> addressArray = new ArrayList<>();
            try {
                addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            } catch (IOException e) {
                address = Constants.UNKNOWN_ADDRESS;
                e.printStackTrace();
            }
            if (addressArray != null) {
                if (!addressArray.isEmpty()) {
                    if (addressArray.get(0) != null) {
                        address += addressArray.get(0).getAddressLine(0).replaceAll(", Polska", "");
                    }
                }
            } else {
                address = Constants.UNKNOWN_ADDRESS;
            }

        } else {
            address = Constants.UNKNOWN_ADDRESS;
        }
        return address;
    }

    @Override
    protected void onPostExecute(String address) {
        super.onPostExecute(address);
        String tag = "";
        switch (tag) {
            case "shopLocation":
                break;
            default:
                if(address.equals(Constants.UNKNOWN_ADDRESS)){
                    callbackView.setText("X: " + position.longitude + ", Y: " + position.latitude);
                } else {
                    callbackView.setText(address);
                }
                break;
        }
    }
}