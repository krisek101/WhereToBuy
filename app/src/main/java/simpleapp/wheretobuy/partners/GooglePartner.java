package simpleapp.wheretobuy.partners;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.AutocompleteAddressesAdapter;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;
import simpleapp.wheretobuy.tasks.onResponseGoogleNearbyShops;

public class GooglePartner {

    private String startLink;
    private MapActivity mapActivity;

    public GooglePartner(String startLink, MapActivity mapActivity) {
        this.startLink = startLink;
        this.mapActivity = mapActivity;
    }

    // method
    public void searchNearbyShops(Shop shop, int radius) {
        String url = getNearbyShopsUrl(shop.getName(), radius);
        requestNearbyShops(url, Constants.TAG_NEARBY_SHOPS, shop);
    }

    public void changeUserLocation(String id) {
        String url = getPlaceDetailsUrl(id);
        requestUserLocationDetails(url, Constants.TAG_USER_LOCATION);
    }

    public void updateShopLocations(String id, ShopLocation shopLocation) {
        String url = getPlaceDetailsUrl(id);
        requestShopDetails(url, Constants.TAG_PLACE_DETAILS, shopLocation);
    }

    public void showAutoCompleteAddresses(String s) {
        String url = getAddressAutocompleteUrl(s);
        requestAutoCompleteAddresses(url, Constants.TAG_SEARCH_ADDRESS_AUTOCOMPLETE);
    }

    // url
    @NonNull
    private String getNearbyShopsUrl(String shopName, int radius) {
        StringBuilder urlString = new StringBuilder();
        urlString.append(startLink);
        urlString.append("nearbysearch/json?language=pl&location=");
        urlString.append(mapActivity.userLocation.latitude + "," + mapActivity.userLocation.longitude + "&radius=" + radius + "&types=store&key=" + Constants.WEB_API_GOOGLE_KEY + "&keyword=");
        try {
            urlString.append(URLEncoder.encode(shopName, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlString.toString();
    }

    private String getPlaceDetailsUrl(String placeId) {
        return "https://maps.googleapis.com/maps/api/place/details/json?language=pl&placeid=" + placeId + "&key=" + Constants.WEB_API_GOOGLE_KEY;
    }

    private String getAddressAutocompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&language=pl&components=country:pl");
        urlString.append("&key=" + Constants.WEB_API_GOOGLE_KEY);
        return urlString.toString();
    }

    // request
    private void requestNearbyShops(String url, final String tag, final Shop shop) {
        Log.i("GOOGLE", url);
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new onResponseGoogleNearbyShops(mapActivity, response, shop, tag).execute();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
            }
        });
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestUserLocationDetails(String url, String tag) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponsePlaceDetails(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestShopDetails(String url, String tag, final ShopLocation shopLocation) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseShopDetails(response, shopLocation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestAutoCompleteAddresses(String url, String tag) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseAutoCompleteAddresses(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    // response
    private void onResponseNearbyShops(JSONObject response, Shop shop) throws JSONException {
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
                    if (name.toLowerCase().replaceAll(".pl", "").replaceAll("\\s+", "").contains(shop.getName().toLowerCase().replaceAll(".pl", "").replaceAll("\\s+", ""))) {
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

                        // add marker to map
                        Bitmap icon;
                        if (openNow) {
                            icon = BitmapFactory.decodeResource(mapActivity.getResources(), R.drawable.shop_marker_open);
                        } else {
                            icon = BitmapFactory.decodeResource(mapActivity.getResources(), R.drawable.shop_marker_closed);
                        }
                        Bitmap bitmap = Bitmap.createScaledBitmap(icon, (int) Math.round(icon.getWidth() * 0.4), (int) Math.round(icon.getHeight() * 0.4), false);
                        Marker marker = mapActivity.mMap.addMarker(new MarkerOptions().position(location).title(shop.getName()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                        shopLocation = new ShopLocation(placeId, name, location, marker, UsefulFunctions.getDistanceBetween(location, mapActivity.userLocation), rating, openNow);
                        shopLocation.setAddress(vicinity);
                        shop.addLocation(shopLocation);
                    }
                }
            }

            // update offers
            for (Offer o : mapActivity.offers) {
                if (o.getShop().getName().equals(shop.getName())) {
                    o.setShop(shop);
                }
            }
        }
    }

    private void onResponsePlaceDetails(JSONObject response) throws JSONException {
        mapActivity.changeLocationDialog.dismiss();
        JSONObject positionObject = response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
        mapActivity.userLocation = new LatLng(positionObject.getDouble("lat"), positionObject.getDouble("lng"));
        mapActivity.setUserLocationMarker();
        if (!mapActivity.shops.isEmpty()) {
            mapActivity.refreshShopLocations();
        }
    }

    private void onResponseShopDetails(JSONObject response, ShopLocation shopLocation) throws JSONException {
        if (response.has("result")) {
            JSONObject placeInfo = response.getJSONObject("result");
            // empty setters
            shopLocation.setWebsite("");
            shopLocation.setPhoneNumber("");
            shopLocation.setOpenHours(new ArrayList<String>());
            shopLocation.setReviews(new JSONArray());

            // real setters
            if (!placeInfo.isNull("website")) {
                shopLocation.setWebsite(placeInfo.getString("website"));
            }
            if (!placeInfo.isNull("formatted_phone_number")) {
                shopLocation.setPhoneNumber(placeInfo.getString("formatted_phone_number"));
            }
            if (!placeInfo.isNull("reviews")) {
                shopLocation.setReviews(placeInfo.getJSONArray("reviews"));
            }
            if (!placeInfo.isNull("opening_hours")) {
                String openHours[] = new String[7];
                for (int i = 0; i < 7; i++) {
                    if (!placeInfo.getJSONObject("opening_hours").getJSONArray("weekday_text").getString(i).isEmpty()) {
                        openHours[i] = placeInfo.getJSONObject("opening_hours").getJSONArray("weekday_text").getString(i);
                    } else {
                        openHours[i] = "null";
                    }
                }
                shopLocation.setOpenHours(Arrays.asList(openHours));
            }
        }
    }

    private void onResponseAutoCompleteAddresses(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("predictions");
        Map<String, String> autocompleteAddresses = new HashMap<>();
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String address = c.getString("description").replaceAll(", Polska", "");
            String place_id = c.getString("place_id");
            autocompleteAddresses.put(place_id, address);
        }
        AutocompleteAddressesAdapter adapter = new AutocompleteAddressesAdapter(R.layout.autocomplete_item, autocompleteAddresses, mapActivity);
        mapActivity.searchLocation.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}