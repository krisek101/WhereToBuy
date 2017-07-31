package simpleapp.wheretobuy.models;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.AutocompleteAdapter;
import simpleapp.wheretobuy.constants.Constants;

public class RequestToQueue {

    private String link;
    private String tag;
    private MapActivity mapActivity;
    private String category;
    private String language;

    public RequestToQueue(String tag, String category, MapActivity mapActivity) {
        this.tag = tag;
        this.category = category;
        this.mapActivity = mapActivity;
        language = Locale.getDefault().getLanguage();
    }

    public RequestToQueue(String tag, MapActivity mapActivity) {
        this.tag = tag;
        this.mapActivity = mapActivity;
        language = Locale.getDefault().getLanguage();
    }

    public void doRequest() {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_AUTOCOMPLETE:
                            onResponseAutocomplete(response);
                            break;
                        case Constants.TAG_CATEGORY:
                            onResponseAutocompleteCategory(response);
                            break;
                        case Constants.TAG_RESULT_DETAILS:
                            onResponseResultDetails(response);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + Constants.NOKAUT_TOKEN);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    public void doRequest(final Shop shop) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_PLACES:
                            Log.i("LINK", link);
                            onResponsePlaces(response, shop);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + Constants.NOKAUT_TOKEN);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void onResponseAutocomplete(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("products");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String title = c.getString("title");
            String id = c.getString("id");
            mapActivity.autoCompleteResults.add(new AutoCompleteResult("product", title, id));
        }
        AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
        mapActivity.searchText.setAdapter(autocompleteAdapter);
        autocompleteAdapter.notifyDataSetChanged();
    }

    private void onResponseAutocompleteCategory(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("categories");
        if (ja.length() < 1) {
            RequestToQueue productsRequest = new RequestToQueue(Constants.TAG_AUTOCOMPLETE, mapActivity);
            productsRequest.setProductAutocompleteUrl(category);
            productsRequest.doRequest();
        } else {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject c = ja.getJSONObject(i);
                String title = c.getString("title");
                String id = c.getString("id");
                mapActivity.autoCompleteResults.add(new AutoCompleteResult("category", title, id));
            }
            AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
            mapActivity.searchText.setAdapter(autocompleteAdapter);
            autocompleteAdapter.notifyDataSetChanged();
        }
    }

    private void onResponseResultDetails(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("offers");
        String shopId = "", shopName = "", shopUrl = "", shopLogoUrl = "";
        Shop shopModel;
        List<Shop> shopsList = new ArrayList<>();
        boolean exists;
        for (int i = 0; i < ja.length(); i++) {
            JSONObject shop = ja.getJSONObject(i).getJSONObject("shop");
            shopName = shop.getString("name");
            if (shop.has("id")) {
                shopId = shop.getString("id");
            }
            if (shop.has("url")) {
                shopUrl = shop.getString("url");
            }
            if (shop.has("url_logo")) {
                shopLogoUrl = shop.getString("url_logo");
            }
            shopModel = new Shop(shopName, shopUrl, shopLogoUrl, shopId);
            exists = false;
            for(Shop s : shopsList){
                if(s.getName().equals(shopModel.getName())){
                    exists = true;
                }
            }
            if (!exists && !shopModel.getId().isEmpty()) {
                shopsList.add(shopModel);
            }
        }
        for(Shop shop : shopsList) {
            RequestToQueue requestToQueue = new RequestToQueue(Constants.TAG_PLACES, mapActivity);
            requestToQueue.setPlacesUrl(shop.getName());
            requestToQueue.doRequest(shop);
        }
    }

    private void onResponsePlaces(JSONObject response, Shop shop) throws JSONException {
        JSONArray ja = response.getJSONArray("results");
        List<LatLng> locations = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            JSONObject locationJSON = c.getJSONObject("geometry").getJSONObject("location");
            LatLng location = new LatLng(locationJSON.getDouble("lat"),locationJSON.getDouble("lng"));
            locations.add(location);
            mapActivity.mMap.addMarker(new MarkerOptions().position(location).title(shop.getName()));
        }
        shop.setLocations(locations);
    }

    public void setProductAutocompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2/products?phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&limit=20&fields=id,title");
        setLink(urlString.toString());
    }

    public void setCategoryAutocompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2/categories?fields=id,title&filter%5Btitle%5D%5Blike%5D=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.category = input;
        setLink(urlString.toString());
    }

    public void setResultDetailsUrl(AutoCompleteResult result) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2");
        if (result.getType().equals("product")) {
            urlString.append("/products/");
            try {
                urlString.append(URLEncoder.encode(result.getId(), "utf8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            urlString.append("/offers?fields=shop.name,shop.id,shop.url,shop.url_logo");
        } else if (result.getType().equals("category")) {
            urlString.append("/offers?fields=shop.name,shop.id,shop.url,shop.url_logo&filter%5Bcategory_id%5D=");
            urlString.append(result.getId());
        }
        setLink(urlString.toString());
    }

    public void setPlacesUrl(String shopName){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=pl&location=");
        urlString.append(mapActivity.userLocation.latitude + "," + mapActivity.userLocation.longitude + "&radius=10000&key=" + Constants.WEB_API_GOOGLE_KEY + "&keyword=");
        try {
            urlString.append(URLEncoder.encode(shopName, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setLink(urlString.toString());
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
