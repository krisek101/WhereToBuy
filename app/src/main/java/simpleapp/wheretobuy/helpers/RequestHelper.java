package simpleapp.wheretobuy.helpers;

import android.os.Handler;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
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
    private int offset = 0;
    private int index = 0;

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
                        case Constants.TAG_AUTOCOMPLETE_BY_CATEGORY:
                            onResponseAutocompleteByCategory(response);
                            break;
                        case Constants.TAG_AUTOCOMPLETE:
                            onResponseAutocompleteByProduct(response, input);
                            break;
                        case Constants.TAG_OFFERS:
                            onResponseOffers(response);
                            break;
                        case Constants.TAG_MORE_PRODUCTS:
                            onMoreProductsResponse(response, input);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                        JSONObject obj = new JSONObject(res);
                        Log.i("ERROR", res);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + Constants.NOKAUT_TOKEN);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void doRequest(final Shop shop) {
        if (!mapActivity.finish) {
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, link, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        switch (tag) {
                            case Constants.TAG_PLACES:
                                //Log.i("LINK", link);
                                if (!mapActivity.finish) {
                                    onResponsePlaces(response, shop);
                                }
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
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            jsonObjRequest.setTag(tag);
            mapActivity.queue.add(jsonObjRequest);
        }
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
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void onMoreProductsResponse(JSONObject response, String input) throws JSONException {
        JSONArray ja = response.getJSONArray("products");
        AutoCompleteResult autoCompleteResult;

        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String title = c.getString("title");
            String id = c.getString("id");
            autoCompleteResult = new AutoCompleteResult("product", title, id);
            if (!mapActivity.autoCompleteResults.contains(autoCompleteResult)) {
                mapActivity.autoCompleteResults.add(autoCompleteResult);
            }
        }

        // queries
        if (offset <= 300) {
            if (ja.length() >= 20) {
                // Go for more products
                Log.i("OFFSET", offset + "");
                RequestHelper rq = new RequestHelper(Constants.TAG_MORE_PRODUCTS, mapActivity);
                rq.setMoreProductsUrl(input, offset);
                rq.doRequest(input);
            } else {
                // No more products, get offers
                mapActivity.requestHelper.index++;
                for (AutoCompleteResult r : mapActivity.autoCompleteResults) {
                    if (!r.getId().equals("all") && !r.getName().equals(input)) {
                        mapActivity.requestHelper.setTag(Constants.TAG_OFFERS);
                        mapActivity.requestHelper.setOffersUrl(r);
                        mapActivity.requestHelper.doRequest("");
                    }
                }
            }
        } else {
            // Maximum quantity of products, get offers
            mapActivity.requestHelper.index++;
            for (AutoCompleteResult r : mapActivity.autoCompleteResults) {
                if (!r.getId().equals("all") && !r.getName().equals(input)) {
                    mapActivity.requestHelper.setTag(Constants.TAG_OFFERS);
                    mapActivity.requestHelper.setOffersUrl(r);
                    mapActivity.requestHelper.doRequest("");
                }
            }
        }
    }

    private void onResponsePlaceDetails(JSONObject response, ShopLocation shopLocation) throws JSONException {
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

    private void onResponseAutocompleteByProduct(JSONObject response, String input) throws JSONException {
        JSONArray ja = response.getJSONArray("products");

        AutoCompleteResult sample = new AutoCompleteResult("product", input, "all");
        if (ja.length() != 0 && !mapActivity.autoCompleteResults.contains(sample)) {
            mapActivity.autoCompleteResults.add(sample);
        }

        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String title = c.getString("title");
            String id = c.getString("id");
            AutoCompleteResult autoCompleteResult = new AutoCompleteResult("product", title, id);
            if (!mapActivity.autoCompleteResults.contains(autoCompleteResult)) {
                mapActivity.autoCompleteResults.add(autoCompleteResult);
            }
        }

        AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
        mapActivity.searchText.setAdapter(autocompleteAdapter);
        autocompleteAdapter.notifyDataSetChanged();
    }

    private void onResponseAutocompleteByCategory(JSONObject response) throws JSONException {
        JSONArray results = response.getJSONArray("categories");
        // if no results, make query for products
        if (results.length() == 0) {
            RequestHelper productsRequest = new RequestHelper(Constants.TAG_AUTOCOMPLETE, mapActivity);
            productsRequest.setProductAutocompleteUrl(category);
            productsRequest.doRequest(category);
        } else {
            // show categories
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String title = result.getString("title");
                String id = result.getString("id");
                AutoCompleteResult autoCompleteResult = new AutoCompleteResult("category", title, id);
                if (!mapActivity.autoCompleteResults.contains(autoCompleteResult)) {
                    mapActivity.autoCompleteResults.add(autoCompleteResult);
                }
            }
            AutocompleteAdapter autocompleteAdapter = new AutocompleteAdapter(R.layout.autocomplete_item, mapActivity.autoCompleteResults, mapActivity);
            mapActivity.searchText.setAdapter(autocompleteAdapter);
            autocompleteAdapter.notifyDataSetChanged();
        }
    }

    private void onResponseOffers(JSONObject response) throws JSONException {
        if (response.has("offers")) {
            JSONArray ja = response.getJSONArray("offers");
            if (ja.length() == 0) {
                mapActivity.setLoading(false);
            } else {
                Shop shopModel;
                Offer offerModel;
                for (int i = 0; i < ja.length(); i++) {
                    // get JSON objects
                    JSONObject offer = ja.getJSONObject(i);
                    JSONObject shop = offer.getJSONObject("shop");

                    // build temp models
                    offerModel = buildTempOffer(offer);
                    shopModel = buildTempShop(shop);

                    // add shop to list, if not exists
                    if (!mapActivity.shops.contains(shopModel) && !shopModel.getId().isEmpty()) {
                        mapActivity.shops.add(shopModel);

                    }

                    // update
                    offerModel.setShop(shopModel);
                    mapActivity.offers.add(offerModel);
                }
            }
        } else {
            mapActivity.setLoading(false);
        }

        // update footer info
        mapActivity.changeFooterInfo();

//        Log.i("PRODUCTS", "All:" + (mapActivity.autoCompleteResults.size() - 1) + ", CURRENT:" + (index) + " - " + mapActivity.autoCompleteResults.get(index).getName());
        if (mapActivity.autoCompleteResults.size() - 1 == index) {
            index = 0;
            for (Shop shop : mapActivity.shops) {
                // get shop locations
                setShopLocations(shop);
            }
            mapActivity.setLoading(false);
        }
        index++;
    }

    private void onResponsePlaces(JSONObject response, Shop shop) throws JSONException {
        JSONArray ja = response.getJSONArray("results");

        if (ja.length() != 0) {
            String name, placeId;
            LatLng location;
            Double rating = 0.0;
            Boolean openNow;
            ShopLocation shopLocation;
            String vicinity = "";

            for (int i = 0; i < ja.length(); i++) {
                JSONObject c = ja.getJSONObject(i);
                if (c.has("name")) {
                    name = c.getString("name");
                    if (name.toLowerCase().contains(shop.getName().toLowerCase())) {
                        JSONObject locationJSON = c.getJSONObject("geometry").getJSONObject("location");
                        location = new LatLng(locationJSON.getDouble("lat"), locationJSON.getDouble("lng"));
                        placeId = c.getString("place_id");
                        if (c.has("rating")) {
                            rating = c.getDouble("rating");
                        }
                        if (c.has("vicinity")) {
                            vicinity = c.getString("vicinity");
                        }
                        openNow = c.getJSONObject("opening_hours").getBoolean("open_now");

                        // add marker to map
                        Marker marker = mapActivity.mMap.addMarker(new MarkerOptions().position(location).title(shop.getName()));
                        shopLocation = new ShopLocation(placeId, name, location, marker, UsefulFunctions.getDistanceBetween(location, mapActivity.userLocation), rating, openNow);
                        shopLocation.setAddress(vicinity);
                        shop.addLocation(shopLocation);
                    }
                }
            }

            // update shop
            for (Offer o : mapActivity.offers) {
                if (o.getShop().getName().equals(shop.getName())) {
                    o.setShop(shop);
                }
            }
        }

        List<Shop> tempShops = new ArrayList<>();
        for (Shop s : mapActivity.shops) {
            if (!s.getLocations().isEmpty()) {
                tempShops.add(s);
            }
        }

        // stop loading on the last element
        if (!tempShops.isEmpty()) {
            if (shop.getId().equals(tempShops.get(tempShops.size() - 1).getId())) {
                mapActivity.changeFooterInfo();
                mapActivity.setLoading(false);
            }
        } else {
            mapActivity.setLoading(false);
        }

        // update footer
        mapActivity.changeFooterInfo();
    }

    private void setProductAutocompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2/products?phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&fields=id,title");
        setLink(urlString.toString());
    }

    public void setMoreProductsUrl(String input, int offset) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2/products?phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&offset=" + offset + "&fields=id,title");
        setLink(urlString.toString());
        this.offset = offset + 20;
    }

    public void setAutocompleteByCategoryUrl(String input) {
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

    public void setOffersUrl(AutoCompleteResult result) {
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

    private void setPlacesUrl(String shopName) {
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

    private void setLink(String link) {
        this.link = link;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    private void setShopLocations(final Shop shop) {
        // check if unacceptable shop
        boolean unacceptable = false;
        for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
            String element = Constants.BLACK_LIST_SHOPS[p];
            if (shop.getName().toLowerCase().equals(element.toLowerCase())) {
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

                            //Log.i("SHOP NAME", shop.getName());
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

    private Offer buildTempOffer(JSONObject offer) throws JSONException {
        String offerClickUrl = "", offerCategory = "", offerTitle = "", offerProducer = "", offerPhotoId = "", offerDesc = "";
        int offerAvailability = 4;
        double offerPrice = 0;

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
        return new Offer(offerAvailability, offerPrice, offerTitle, offerCategory, offerDesc, offerClickUrl, offerProducer, offerPhotoId);
    }

    private Shop buildTempShop(JSONObject shop) throws JSONException {
        String shopId = "", shopName = "", shopUrl = "", shopLogoUrl = "";

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
        return new Shop(shopName, shopUrl, shopLogoUrl, shopId);
    }
}