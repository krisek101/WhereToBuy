package simpleapp.wheretobuy.partners;

import android.support.annotation.NonNull;
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
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.tasks.onResponseNokautOfferTask;

public class NokautPartner {

    private String startLink;
    private MapActivity mapActivity;
    private int offset = 0;

    public NokautPartner(String startLink, MapActivity mapActivity) {
        this.startLink = startLink;
        this.mapActivity = mapActivity;
    }

    // method
    public void showAutoCompleteProducts(String input) {
        String url = getProductsUrl(input, -1);
        requestShowAutoCompleteProducts(url, Constants.TAG_SHOW_AUTOCOMPLETE_PRODUCTS, input);
    }

    public void getMoreProducts(String name, int offset) {
        String url = getProductsUrl(name, offset);
        this.offset = offset + 20;
        requestGetMoreProducts(url, Constants.TAG_MORE_PRODUCTS, name);
    }

    public void getOffers(AutoCompleteResult result) {
        String url = getOffersUrl(result);
        requestGetOffers(url, Constants.TAG_OFFERS);
    }

    // url
    @NonNull
    private String getProductsUrl(String input, int offset) {
        StringBuilder urlString = new StringBuilder();
        urlString.append(startLink);
        urlString.append("products?quality=100&phrase=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&fields=id,title");
        if (offset != -1) {
            urlString.append("&offset=" + offset);
        }
        return urlString.toString();
    }

    @NonNull
    private String getOffersUrl(AutoCompleteResult result) {
        StringBuilder urlString = new StringBuilder();
        urlString.append(startLink);
        if (result.getType().equals("product")) {
            urlString.append("products/");
            try {
                urlString.append(URLEncoder.encode(result.getId(), "utf8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            urlString.append("/offers?fields=title,shop.name,shop.id,shop.url,shop.url_logo,availability,category,description_short,price,producer,photo_id,click_url");
        } else if (result.getType().equals("category")) {
            urlString.append("offers?fields=title,shop.name,shop.id,shop.url,shop.url_logo,availability,category,description_short,price,producer,photo_id,click_url&filter%5Bcategory_id%5D=");
            urlString.append(result.getId());
        }
        return urlString.toString();
    }

    // request
    private void requestShowAutoCompleteProducts(String url, final String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mapActivity.searchText.hideProgress();
                    onResponseShowAutoCompleteByProduct(response, input);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.skapiecHelper.showAutoCompleteByProduct(input);
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

    private void requestGetMoreProducts(String url, final String tag, final String input) {
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseGetMoreProducts(response, input);
                    mapActivity.loadingHelper.changeLoader(-1, tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
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

    private void requestGetOffers(String url, final String tag) {
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onResponseNokautOfferTask task = new onResponseNokautOfferTask(mapActivity, response, tag);
                task.execute();
                mapActivity.nokautOffersTasks.add(task);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
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

    // response
    private void onResponseShowAutoCompleteByProduct(JSONObject response, String input) throws JSONException {
        JSONArray ja = response.getJSONArray("products");
        if (ja.length() != 0) {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject c = ja.getJSONObject(i);
                String title = c.getString("title");
                String id = c.getString("id");
                AutoCompleteResult autoCompleteResult = new AutoCompleteResult("product", title, id);
                if (!mapActivity.autoCompleteResults.contains(autoCompleteResult)) {
                    mapActivity.autoCompleteResults.add(autoCompleteResult);
                }
            }

            // find most common suggestion
            List<String> names = new ArrayList<>();
            String[] splitInput = input.toLowerCase().split("\\s+");
            String[] splitName;
            String name, finalName;
            for (AutoCompleteResult acr : mapActivity.autoCompleteResults) {
                name = acr.getName().toLowerCase();
                splitName = name.split("\\s+");
                finalName = "";
                for (String inputPart : splitInput) {
                    for (String namePart : splitName) {
                        if (namePart.contains(inputPart)) {
                            if (!finalName.contains(namePart)) {
                                finalName += namePart + " ";
                            }
                        }
                    }
                }
                if (!finalName.isEmpty()) {
                    names.add(finalName);
                }
            }

            AutoCompleteResult sample = new AutoCompleteResult("product", input, "all");
            if (!names.isEmpty()) {
                sample.setName(UsefulFunctions.getMostCommonString(names));
            }
            if (mapActivity.autoCompleteResults.contains(sample)) {
                mapActivity.autoCompleteResults.remove(sample);
            }
            mapActivity.autoCompleteResults.add(0, sample);
            mapActivity.searchText.swapSuggestions(mapActivity.autoCompleteResults);
        } else {
            mapActivity.skapiecHelper.showAutoCompleteByProduct(input);
        }
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
                this.getOffers(autoCompleteResult);
            }
        }

        // queries
//        AutoCompleteResult r;
        if (offset <= 1000) {
            if (ja.length() >= 20) {
                Log.i("OFFSET", offset + "");
                mapActivity.nokautHelper.getMoreProducts(input, offset);
            }
        }
//            } else {
//                // No more products, get offers
//                for (int i = 0; i < mapActivity.autoCompleteResults.size() - 1; i++) {
//                    r = mapActivity.autoCompleteResults.get(i);
//                    if (!r.getType().equals(Constants.OFFER_SKAPIEC) && !r.getId().equals("all")) {
//                        this.getOffers(r);
//                    }
//                }
//            }
//        } else {
//            // Maximum quantity of products, get offers
//            for (int i = 0; i < mapActivity.autoCompleteResults.size() - 1; i++) {
//                r = mapActivity.autoCompleteResults.get(i);
//                if (!r.getType().equals(Constants.OFFER_SKAPIEC) && !r.getId().equals("all")) {
//                    this.getOffers(r);
//                }
//            }
//        }
    }
}