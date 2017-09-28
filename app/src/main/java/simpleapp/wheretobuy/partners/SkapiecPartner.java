package simpleapp.wheretobuy.partners;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.tasks.onResponseSkapiecOfferTask;

public class SkapiecPartner {

    private String startLink;
    private MapActivity mapActivity;
    private String basicAuth = "Basic " + Base64.encodeToString((Constants.SKAPIEC_LOGIN + ":" + Constants.SKAPIEC_PASS).getBytes(), Base64.NO_WRAP);
    private List<String> categories = new ArrayList<>();
//    private List<Offer> offers = new ArrayList<>();

    public SkapiecPartner(String startLink, MapActivity mapActivity) {
        this.startLink = startLink;
        this.mapActivity = mapActivity;
    }

    // method
    public void searchMostCommonCategory(String input, int offset) {
        String url = getSearchOffersFilterUrl(input, offset);
        Log.i("LINK", url);
        requestSearchMostCommonCategory(url, Constants.TAG_MOST_COMMON_CATEGORY_SKAPIEC, input);
    }

    private void searchOffersByCategoryId(String input, int offset, String categoryID) {
        if (categoryID != null) {
            String url = getSearchOffersByCategoryIdUrl(input, offset, categoryID);
            Log.i("LINK", url);
            requestSearchOffersWithBestCategory(url, Constants.TAG_PRODUCTS_SKAPIEC);
        }
    }

    private void getOfferInfo(Offer o) {
        String url = getBestPriceOffersUrl(o.getId());
        requestOfferInfo(url, Constants.TAG_OFFERS_SKAPIEC, o);
    }

    public void showAutoCompleteByProduct(String input) {
        String url = getSearchOffersFilterUrl(input, 0);
        requestShowAutoCompleteProducts(url, Constants.TAG_SHOW_AUTOCOMPLETE_PRODUCTS_SKAPIEC, input);
    }

    // url
    @NonNull
    private String getSearchOffersFilterUrl(String input, int offset) {
        String u = startLink + "searchOffersFilters.json?amount=20&offset=" + offset + "&q=";
        try {
            u += URLEncoder.encode(input, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return u;
    }

    @NonNull
    private String getSearchOffersByCategoryIdUrl(String input, int offset, String categoryID) {
        String u = startLink + "searchOffersFilters.json?amount=20&offset=" + offset + "&q=";
        try {
            u += URLEncoder.encode(input, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        u += "&id_category=" + categoryID;
        return u;
    }

//    @NonNull
//    private String getSearchOffersCategoryUrl(int offset) {
//        return startLink + "listProducts.json?amount=20&offset=" + offset + "&category=" + categoryId;
//    }

    @NonNull
    private String getBestPriceOffersUrl(String id) {
        return startLink + "getOffersBestPrice.json?id_skapiec=" + id + "&amount=20";
    }

    // request
    private void requestSearchMostCommonCategory(String url, final String tag, final String input) {
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseSearchMostCommonCategory(response, input);
                    mapActivity.loadingHelper.changeLoader(-1, tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", basicAuth);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestShowAutoCompleteProducts(String url, final String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseShowAutoCompleteProducts(response, input);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AutoCompleteResult err = new AutoCompleteResult("error", "Wystąpił błąd, spróbuj ponownie później.", "noResponseFromServer");
                List<AutoCompleteResult> errs = new ArrayList<>();
                errs.add(err);
                mapActivity.searchText.swapSuggestions(errs);
                mapActivity.searchText.hideProgress();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", basicAuth);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestSearchOffersWithBestCategory(String url, final String tag) {
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseSearchOffersByCategory(response);
                    mapActivity.loadingHelper.changeLoader(-1, tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", basicAuth);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestOfferInfo(String url, final String tag, final Offer o) {
        mapActivity.loadingHelper.changeLoader(1, tag);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                new onResponseSkapiecOfferTask(mapActivity, response, o, tag).execute();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mapActivity.loadingHelper.changeLoader(-1, tag);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", basicAuth);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    // response
    private void onResponseSearchMostCommonCategory(JSONObject response, String input) throws JSONException {
        JSONArray results = response.getJSONObject("components").getJSONArray("component");
        JSONObject pagination = response.getJSONObject("pagination");
        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            String id = jsonObject.getString("id_skapiec");
            String category = jsonObject.getString("link").replace("https://www.skapiec.pl/cat/", "").replaceAll("/comp/" + id, "");
            categories.add(category);
        }

        if (pagination.has("next") && pagination.getInt("offset") == 0) {
            // get results from the second page
            this.searchMostCommonCategory(input, 20);
        } else {
            // start searching offers by best popular category ID
            this.searchOffersByCategoryId(input, 0, UsefulFunctions.getMostCommonString(categories));
            categories.clear();
        }
    }

    private void onResponseShowAutoCompleteProducts(JSONObject response, String input) throws JSONException {
        JSONArray results = response.getJSONObject("components").getJSONArray("component");
        if (results.length() != 0) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject c = results.getJSONObject(i);
                String title = c.getString("name");
                String id = c.getString("id_skapiec");
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
            AutoCompleteResult error = new AutoCompleteResult("error", "Brak podpowiedzi.", "noSuggestions");
            mapActivity.autoCompleteResults.add(error);
            mapActivity.searchText.swapSuggestions(mapActivity.autoCompleteResults);
        }
    }

    private void onResponseSearchOffersByCategory(JSONObject response) throws JSONException {
        List<Offer> tempOffers = getOffersListFromJSON(response);
//        offers.addAll(tempOffers);
//        int offset = response.getJSONObject("pagination").getInt("offset");
//        if (response.getJSONObject("pagination").has("next") && offset < 20) {
//            searchOffersByCategoryId(input, offset + 20);
//        } else {
//            boolean finish = false;
//            for (int i = 0; i < 10; i++) {
//                if (i == 9) {
//                    finish = true;
//                }
//                this.getOfferInfo(offers.get(i), finish);
//            }
//        }
        for (Offer offer : tempOffers) {
            this.getOfferInfo(offer);
        }
    }

    // others
    private List<Offer> getOffersListFromJSON(JSONObject response) throws JSONException {
        JSONArray results = response.getJSONObject("components").getJSONArray("component");
        List<Offer> offers = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            String name = jsonObject.getString("name");
            String id = jsonObject.getString("id_skapiec");
            String categoryID = jsonObject.getString("link").replace("https://www.skapiec.pl/cat/", "").replaceAll("/comp/" + id, "");
            String link = jsonObject.getString("link");
            String photo = jsonObject.getString("small_photo");
            Offer offer = new Offer(Constants.OFFER_SKAPIEC, 4, name, link, categoryID, photo, id);
            if (!mapActivity.offers.contains(offer)) {
                offers.add(offer);
            }
        }
        return offers;
    }
}