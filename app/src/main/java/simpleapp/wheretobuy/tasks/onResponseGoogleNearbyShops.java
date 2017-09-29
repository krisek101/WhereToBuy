package simpleapp.wheretobuy.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;

public class onResponseGoogleNearbyShops extends AsyncTask<Void, Void, Void> {

    private MapActivity mapActivity;
    private JSONObject response;
    private String tag;
    private Shop shop;

    public onResponseGoogleNearbyShops(MapActivity mapActivity, JSONObject response, Shop shop, String tag) {
        this.mapActivity = mapActivity;
        this.response = response;
        this.shop = shop;
        this.tag = tag;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            JSONArray ja = response.getJSONArray("results");
            if (ja.length() != 0) {
                String name, placeId, vicinity = "";
                LatLng location;
                Double rating = 0.0;
                Boolean openNow = false;
                ShopLocation shopLocation;

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject c = ja.getJSONObject(i);
                    if (c.has("name")) {
                        name = c.getString("name");
                        if (name.toLowerCase().replaceAll(".pl","").replaceAll(".com", "").replaceAll("\\s+","").contains(shop.getName().toLowerCase().replaceAll(".pl","").replaceAll(".com", "").replaceAll("\\s+",""))) {
                            JSONObject locationJSON = c.getJSONObject("geometry").getJSONObject("location");
                            location = new LatLng(locationJSON.getDouble("lat"), locationJSON.getDouble("lng"));

                            placeId = c.getString("place_id");

                            if (c.has("rating")) {
                                rating = c.getDouble("rating");
                            }
                            if (c.has("vicinity")) {
                                vicinity = c.getString("vicinity");
                            }
                            if (c.has("opening_hours")) {
                                openNow = c.getJSONObject("opening_hours").getBoolean("open_now");
                            }

                            // add shopLocation to map
                            shopLocation = new ShopLocation(placeId, name, location, null, UsefulFunctions.getDistanceBetween(location, mapActivity.userLocation), rating, openNow);
                            shopLocation.setAddress(vicinity);
                            shop.addLocation(shopLocation);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        for(ShopLocation shopLocation : shop.getLocations()) {
            Bitmap icon;
            if (shopLocation.isOpenNow()) {
                icon = BitmapFactory.decodeResource(mapActivity.getResources(), R.drawable.shop_marker_open);
            } else {
                icon = BitmapFactory.decodeResource(mapActivity.getResources(), R.drawable.shop_marker_closed);
            }
            Bitmap bitmap = Bitmap.createScaledBitmap(icon, (int) Math.round(icon.getWidth() * 0.4), (int) Math.round(icon.getHeight() * 0.4), false);
            Marker marker = mapActivity.mMap.addMarker(new MarkerOptions().position(shopLocation.getLocation()).title(shop.getName()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            shopLocation.setMarker(marker);
        }

        // update offers
        for (Offer o : mapActivity.offers) {
            if (o.getShop().getName().equals(shop.getName())) {
                o.setShop(shop);
            }
        }
        mapActivity.offersAdapter.notifyDataSetChanged();
        mapActivity.loadingHelper.changeLoader(-1, tag);
    }
}