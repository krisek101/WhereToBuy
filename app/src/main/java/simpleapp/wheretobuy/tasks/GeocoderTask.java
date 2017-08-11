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

public class GeocoderTask extends AsyncTask<Void, Void, String> {

    private MapActivity mapActivity;
    private LatLng position;
    private String tag = "";
    private TextView callbackView;

    public GeocoderTask(MapActivity mapActivity, LatLng position, TextView callbackView) {
        this.mapActivity = mapActivity;
        this.position = position;
        this.callbackView = callbackView;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String address = Constants.UNKNOWN_ADDRESS;
        if (UsefulFunctions.isOnline(mapActivity)) {
            Geocoder geocoder = new Geocoder(mapActivity, Locale.getDefault());
            List<Address> addressArray = new ArrayList<>();
            int failed = 0;
            while (address.equals(Constants.UNKNOWN_ADDRESS) && failed < 2) {
                address = "";
                try {
                    addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressArray != null) {
                    if (!addressArray.isEmpty()) {
                        if (addressArray.get(0) != null) {
                            address += addressArray.get(0).getAddressLine(0);
                            if (addressArray.get(0).getLocality() != null) {
                                address += ", " + addressArray.get(0).getLocality();
                            }
                        }
                    }
                } else {
                    address = Constants.UNKNOWN_ADDRESS;
                    failed++;
                }
            }
        } else {
            address = Constants.UNKNOWN_ADDRESS;
        }
        return address;
    }

    @Override
    protected void onPostExecute(String address) {
        super.onPostExecute(address);
        switch (tag) {
            case "shopLocation":
                break;
            default:
                callbackView.setText(address);
                break;
        }
    }
}