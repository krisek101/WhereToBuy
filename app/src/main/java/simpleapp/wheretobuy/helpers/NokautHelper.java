package simpleapp.wheretobuy.helpers;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class NokautHelper {

    private String startLink;
    private MapActivity mapActivity;
    private int offset = 0;

    public NokautHelper(String startLink, MapActivity mapActivity) {
        this.startLink = startLink;
        this.mapActivity = mapActivity;
    }

    // method
    public void showAutoCompleteCategories(String input) {
        String url = getAutoCompleteCategoriesUrl(input);
        requestAutoCompleteCategories(url, Constants.TAG_AUTOCOMPLETE_BY_CATEGORY, input);
    }

    private void showAutoCompleteProducts(String input) {
        String url = getAutoCompleteProductsUrl(input);
        requestShowAutoCompleteProducts(url, Constants.TAG_AUTOCOMPLETE_BY_PRODUCT, input);
    }

    public void getAutoCompleteProducts(AutoCompleteResult autoCompleteResult) {
        String url = getAutoCompleteProductsUrl(autoCompleteResult.getName());
        requestAutoCompleteProducts(url, Constants.TAG_AUTOCOMPLETE_BY_PRODUCT, autoCompleteResult);
    }

    public void getMoreProducts(String name, int offset) {
        String url = getMoreProductsUrl(name, offset);
        this.offset = offset + 20;
        requestGetMoreProducts(url, Constants.TAG_MORE_PRODUCTS, name);
    }

    public void getOffers(AutoCompleteResult result, boolean finish) {
        String url = getOffersUrl(result);
        requestGetOffers(url, Constants.TAG_OFFERS, finish);
    }

    // url
    @NonNull
    private String getAutoCompleteCategoriesUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append(startLink);
        urlString.append("categories?fields=id,title&filter%5Btitle%5D%5Blike%5D=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlString.toString();
    }

    @NonNull
    private String getAutoCompleteProductsUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append(startLink);
        urlString.append("products?quality=100&phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&fields=id,title");
        return urlString.toString();
    }

    @NonNull
    private String getMoreProductsUrl(String input, int offset) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://nokaut.io/api/v2/products?quality=100&phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&offset=" + offset + "&fields=id,title");
        return urlString.toString();
    }

    @NonNull
    private String getOffersUrl(AutoCompleteResult result) {
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
        return urlString.toString();
    }

    // request
    private void requestAutoCompleteCategories(String url, String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseAutoCompleteByCategory(response, input);
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

    private void requestShowAutoCompleteProducts(String url, String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseShowAutoCompleteByProduct(response, input);
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

    private void requestAutoCompleteProducts(String url, String tag, final AutoCompleteResult autoCompleteResult) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseAutoCompleteByProduct(response, autoCompleteResult);
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

    private void requestGetMoreProducts(String url, String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseGetMoreProducts(response, input);
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

    private void requestGetOffers(String url, String tag, final boolean finish) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseGetOffers(response, finish);
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

    // on response
    private void onResponseAutoCompleteByCategory(JSONObject response, String input) throws JSONException {
        JSONArray results = response.getJSONArray("categories");
        // if no results, make query for products
        if (results.length() == 0) {
            this.showAutoCompleteProducts(input);
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
            mapActivity.searchText.swapSuggestions(mapActivity.autoCompleteResults);
        }
    }

    private void onResponseShowAutoCompleteByProduct(JSONObject response, String input) throws JSONException {
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
        mapActivity.searchText.swapSuggestions(mapActivity.autoCompleteResults);
    }

    private void onResponseAutoCompleteByProduct(JSONObject response, AutoCompleteResult autoCompleteResult) throws JSONException {
        JSONArray ja = response.getJSONArray("products");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject c = ja.getJSONObject(i);
            String title = c.getString("title");
            String id = c.getString("id");
            AutoCompleteResult autoCompleteResult1 = new AutoCompleteResult("product", title, id);
            if (!mapActivity.autoCompleteResults.contains(autoCompleteResult1)) {
                mapActivity.autoCompleteResults.add(autoCompleteResult1);
            }
        }
        mapActivity.queryOffers(autoCompleteResult);
    }

    private void onResponseGetMoreProducts(JSONObject response, String input) throws JSONException {
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
        boolean finish = false;
        if (offset <= 1000) {
            if (ja.length() >= 20) {
                Log.i("OFFSET", offset + "");
                mapActivity.nokautHelper.getMoreProducts(input, offset);
            } else {
                // No more products, get offers
                for (int i = 0; i < mapActivity.autoCompleteResults.size() - 1; i++) {
                    AutoCompleteResult r = mapActivity.autoCompleteResults.get(i);
                    if (!r.getId().equals("all") && !r.getName().equals(input)) {
                        if (i == mapActivity.autoCompleteResults.size()-2){
                            finish = true;
                        }
                        this.getOffers(r, finish);
                    }
                }
            }
        } else {
            // Maximum quantity of products, get offers
            for (int i = 0; i < mapActivity.autoCompleteResults.size() - 1; i++) {
                AutoCompleteResult r = mapActivity.autoCompleteResults.get(i);
                if (!r.getId().equals("all") && !r.getName().equals(input)) {
                    if (i == mapActivity.autoCompleteResults.size()-2){
                        finish = true;
                    }
                    this.getOffers(r, finish);
                }
            }
        }
    }

    private void onResponseGetOffers(JSONObject response, boolean finish) throws JSONException {
        List<Shop> goodShops = new ArrayList<>();
        if (response.has("offers")) {
            JSONArray ja = response.getJSONArray("offers");
            if (ja.length() != 0) {
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
        }

        // update footer info
        mapActivity.changeFooterInfo();

        // search shops with locations near user
        if (finish) {
            boolean isOnBlackList;
            for (Shop shop : mapActivity.shops) {
                // check if shop is on black list
                isOnBlackList = false;
                for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
                    if (shop.getName().toLowerCase().equals(Constants.BLACK_LIST_SHOPS[p].toLowerCase())) {
                        isOnBlackList = true;
                    }
                }

                // if shop is not on black list
                if (!isOnBlackList) {
                    goodShops.add(shop);
                }
            }

            if (goodShops.isEmpty()) {
                mapActivity.setLoading(false);
            } else {
                boolean finish2;
                for (int i = 0; i < goodShops.size() - 1; i++) {
                    Shop shop = goodShops.get(i);
                    finish2 = i == goodShops.size() - 2;
                    setShopLocations(shop, finish2);
                }
            }
        }
    }

    // Others
    public void setShopLocations(final Shop shop, final boolean finish) {
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

                        mapActivity.googleHelper.searchNearbyShops(shop, Constants.SEARCH_RADIUS, finish);

                        h.removeCallbacks(runnable[0]);
                    }
                }
                runnable[0] = this;
                h.postDelayed(runnable[0], delay);
            }
        }, delay);
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