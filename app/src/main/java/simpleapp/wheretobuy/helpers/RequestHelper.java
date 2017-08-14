package simpleapp.wheretobuy.helpers;

import android.os.Handler;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.AutocompleteAdapter;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;

public class RequestHelper {

    private String link;
    private String tag;
    private MapActivity mapActivity;
    private String category;
    private String language;
    private Shop shop;


    public RequestHelper(String tag, MapActivity mapActivity) {
        this.tag = tag;
        this.mapActivity = mapActivity;
        language = Locale.getDefault().getLanguage();
    }

    public void doRequest(final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_AUTOCOMPLETE:
                            onResponseAutocomplete(response, input);
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

    public void doRequest(final ShopLocation shopLocation) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    switch (tag) {
                        case Constants.TAG_PLACE_DETAILS:
                            onResponsePlaceDetails(response, shopLocation);
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

    private void onResponsePlaceDetails(JSONObject response, ShopLocation shopLocation) throws JSONException {
        JSONObject placeInfo = response.getJSONObject("result");
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

        if (!placeInfo.isNull("photos")) {
            String photos[] = new String[placeInfo.getJSONArray("photos").length()];
            for (int i = 0; i < placeInfo.getJSONArray("photos").length(); i++) {
                photos[i] = placeInfo.getJSONArray("photos").getJSONObject(i).getString("photo_reference");
            }
            shopLocation.setPhotos(Arrays.asList(photos));
        }
    }

    private void onResponseAutocomplete(JSONObject response, String input) throws JSONException {
        JSONArray ja = response.getJSONArray("products");
        boolean exists;
        mapActivity.autoCompleteResults.add(new AutoCompleteResult("product", input, "all"));

        for (int i = 0; i < ja.length(); i++) {
            exists = false;
            JSONObject c = ja.getJSONObject(i);
            String title = c.getString("title");
            String id = c.getString("id");
            for (AutoCompleteResult acR : mapActivity.autoCompleteResults) {
                if (acR.getName().equals(title)) {
                    exists = true;
                }
            }
            if (!exists) {
                mapActivity.autoCompleteResults.add(new AutoCompleteResult("product", title, id));
            }
        }
        AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
        mapActivity.searchText.setAdapter(autocompleteAdapter);
        autocompleteAdapter.notifyDataSetChanged();
    }

    private void onResponseAutocompleteCategory(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("categories");
        if (ja.length() < 1) {
            RequestHelper productsRequest = new RequestHelper(Constants.TAG_AUTOCOMPLETE, mapActivity);
            productsRequest.setProductAutocompleteUrl(category);
            productsRequest.doRequest(category);
        } else {
            boolean exists;
            for (int i = 0; i < ja.length(); i++) {
                exists = false;
                JSONObject c = ja.getJSONObject(i);
                String title = c.getString("title");
                String id = c.getString("id");
                for (AutoCompleteResult acR : mapActivity.autoCompleteResults) {
                    if (acR.getName().equals(title)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    mapActivity.autoCompleteResults.add(new AutoCompleteResult("category", title, id));
                }
            }
            AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
            mapActivity.searchText.setAdapter(autocompleteAdapter);
            autocompleteAdapter.notifyDataSetChanged();
        }
    }

    private void onResponseResultDetails(JSONObject response) throws JSONException {
        JSONArray ja = response.getJSONArray("offers");
        String shopId = "", shopName = "", shopUrl = "", shopLogoUrl = "", offerClickUrl = "", offerCategory = "", offerTitle = "", offerProducer = "", offerPhotoId = "", offerDesc = "";
        int offerAvailability = 4;
        double offerPrice = 0;
        Shop shopModel = null;
        Offer offerModel;
        boolean exists;

        for (int i = 0; i < ja.length(); i++) {
            JSONObject offer = ja.getJSONObject(i);
            JSONObject shop = offer.getJSONObject("shop");
            // get offer details
            if (offer.has("availability")) {
                offerAvailability = offer.getInt("availability");
            }
            if (offer.has("price")) {
                offerPrice = offer.getDouble("price");
            }
            if (offer.has("title")) {
                offerTitle = Html.fromHtml(offer.getString("title")).toString();
            }
            if (offer.has("click_url")) {
                offerClickUrl = offer.getString("click_url");
            }
            if (offer.has("category")) {
                offerCategory = offer.getString("category");
            }
            if (offer.has("producer")) {
                offerProducer = offer.getString("producer");
            }
            if (offer.has("description_short")) {
                offerDesc = offer.getString("description_short");
            }
            if (offer.has("photo_id")) {
                offerPhotoId = offer.getString("photo_id");
            }
            offerModel = new Offer(offerAvailability, offerPrice, offerTitle, offerCategory, offerDesc, offerClickUrl, offerProducer, offerPhotoId);

            // get shop details
            if (shop.has("name")) {
                shopName = shop.getString("name");
            }
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
            for (Shop s : mapActivity.shops) {
                if (s.getName().equals(shopModel.getName())) {
                    exists = true;
                }
            }
            if (!exists && !shopModel.getId().isEmpty()) {
                mapActivity.shops.add(shopModel);
                // set shops locations
                setShopLocations(shopModel);
            }

            offerModel.setShop(shopModel);
            mapActivity.offers.add(offerModel);
        }
    }

    private void onResponsePlaces(JSONObject response, Shop shop) throws JSONException {
        JSONArray ja = response.getJSONArray("results");
        String name, placeId;
        LatLng location;
        Double rating;
        Boolean openNow;
        ShopLocation shopLocation;

        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            if (c.has("name")) {
                name = c.getString("name");
                if (name.toLowerCase().replaceAll("\\s+", "").contains(shop.getName().toLowerCase().replaceAll("\\s+", ""))) {
                    JSONObject locationJSON = c.getJSONObject("geometry").getJSONObject("location");
                    location = new LatLng(locationJSON.getDouble("lat"), locationJSON.getDouble("lng"));
                    placeId = c.getString("place_id");
                    rating = c.getDouble("rating");
                    openNow = c.getJSONObject("opening_hours").getBoolean("open_now");

                    Marker marker = mapActivity.mMap.addMarker(new MarkerOptions().position(location).title(shop.getName()));
                    shopLocation = new ShopLocation(placeId, name, location, marker, UsefulFunctions.getDistanceBetween(location, mapActivity.userLocation), rating, openNow);
                    shop.addLocation(shopLocation);
                }
            }
        }

        // if no results, try without ".pl" suffix
        if (ja.length() == 0 && shop.getName().toLowerCase().contains(".pl")) {
            shop.setName(shop.getName().toLowerCase().replace(".pl", ""));
            setShopLocations(shop);
        }

        // update shop
        for (Offer o : mapActivity.offers) {
            if (o.getShop().getName().equals(shop.getName())) {
                o.setShop(shop);
            }
        }

        // stop loading on the last element
        if (!mapActivity.shops.isEmpty()) {
            if (shop.getId().equals(mapActivity.shops.get(mapActivity.shops.size() - 1).getId())) {
                mapActivity.setLoading(false);
            }
        } else {
            mapActivity.setLoading(false);
        }

        // update footer
        mapActivity.changeFooterInfo();
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
            urlString.append("/offers?fields=title,shop.name,shop.id,shop.url,shop.url_logo,availability,category,description_short,price,producer,photo_id,click_url");
        } else if (result.getType().equals("category")) {
            urlString.append("/offers?fields=title,shop.name,shop.id,shop.url,shop.url_logo,availability,category,description_short,price,producer,photo_id,click_url&filter%5Bcategory_id%5D=");
            urlString.append(result.getId());
        }
        setLink(urlString.toString());
    }

    public void setPlacesUrl(String shopName) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=pl&location=");
        urlString.append(mapActivity.userLocation.latitude + "," + mapActivity.userLocation.longitude + "&radius=10000&types=store&key=" + Constants.WEB_API_GOOGLE_KEY + "&keyword=");
        try {
            urlString.append(URLEncoder.encode(shopName, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setLink(urlString.toString());
    }

    public void setPlaceDetailsUrl(ShopLocation shopLocation) {
        setLink("https://maps.googleapis.com/maps/api/place/details/json?language=" + language + "&placeid=" + shopLocation.getId() + "&key=" + Constants.WEB_API_GOOGLE_KEY);
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

    private List<Offer> getOffersFromSelectedShop(Shop shop) {
        List<Offer> returnOffers = new ArrayList<>();
        for (Offer offer : mapActivity.offers) {
            if (offer.getShop().equals(shop)) {
                returnOffers.add(offer);
            }
        }
        return returnOffers;
    }

    private void setShopLocations(final Shop shop) {
        // check if unacceptable shop
        boolean unacceptable = false;
        for (int p = 0; p < Constants.UNACCEPTABLE_SHOPS.length; p++) {
            String element = Constants.UNACCEPTABLE_SHOPS[p];
            if (shop.getName().toLowerCase().equals(element)) {
                unacceptable = true;
            }
        }

        if (!unacceptable) {
            final Handler h = new Handler();
            final int delay = 1000;
            final Runnable[] runnable = new Runnable[1];
            final boolean[] already = {true};
            h.postDelayed(new Runnable() {
                public void run() {
                    if (mapActivity.userLocation != null) {
                        // get shops locations
                        if (already[0]) {
                            already[0] = false;

                            Log.i("SHOP NAME", shop.getName());
                            RequestHelper requestToQueue = new RequestHelper(Constants.TAG_PLACES, mapActivity);
                            requestToQueue.setPlacesUrl(shop.getName());
                            requestToQueue.doRequest(shop);

                            h.removeCallbacks(runnable[0]);
                        }
                    }
                    runnable[0] = this;
                    h.postDelayed(runnable[0], delay);
                }
            }, delay);
        }
    }
}