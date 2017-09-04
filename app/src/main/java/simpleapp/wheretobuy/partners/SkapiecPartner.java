package simpleapp.wheretobuy.partners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class SkapiecPartner {

    private String startLink;
    private MapActivity mapActivity;
    private String basicAuth = "Basic " + Base64.encodeToString((Constants.SKAPIEC_LOGIN + ":" + Constants.SKAPIEC_PASS).getBytes(), Base64.NO_WRAP);
    public String bestCategory;
    private List<Offer> offers = new ArrayList<>();

    public SkapiecPartner(String startLink, MapActivity mapActivity) {
        this.startLink = startLink;
        this.mapActivity = mapActivity;
    }

    // method
    public void searchMostCommonCategory(String input) {
        bestCategory = null;
        String url = getSearchOffersFilterUrl(input, 0);
        Log.i("LINK", url);
        requestSearchMostCommonCategory(url, Constants.TAG_PRODUCTS_SKAPIEC, input);
    }

//    public void searchCategory(String input, int departmentId) {
//        String url = getSearchCategoryUrl(departmentId);
//        requestSearchCategory(url, Constants.TAG_PRODUCTS_SKAPIEC, input, departmentId);
//    }

    private void searchOffersByCategory(String input, int offset) {
        String url = getSearchOffersFilterUrl(input, offset);
        Log.i("LINK", url);
        requestSearchOffersByCategory(url, Constants.TAG_PRODUCTS_SKAPIEC, input);
    }

    private void getOfferInfo(Offer o, boolean finish) {
        String url = getBestPriceOffersUrl(o.getId());
        requestOfferInfo(url, Constants.TAG_OFFERS_SKAPIEC, o, finish);
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
        if (bestCategory != null) {
            u += "&id_category=" + bestCategory;
        }
        return u;
    }

//    @NonNull
//    private String getSearchOffersCategoryUrl(int offset) {
//        return startLink + "listProducts.json?amount=20&offset=" + offset + "&category=" + categoryId;
//    }

    @NonNull
    private String getSearchCategoryUrl(int id) {
        return startLink + "listCategories.json?department=" + id;
    }

    @NonNull
    private String getBestPriceOffersUrl(String id) {
        return startLink + "getOffersBestPrice.json?id_skapiec=" + id + "&amount=20";
    }

    // request
    private void requestSearchMostCommonCategory(String url, String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseSearchMostCommonCategory(response, input);
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
                headers.put("Authorization", basicAuth);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestSearchOffersByCategory(String url, String tag, final String input) {
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseSearchOffersByCategory(response, input);
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
                headers.put("Authorization", basicAuth);
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

    private void requestOfferInfo(String url, String tag, final Offer o, final boolean finish) {
        Log.i("Link 2", url);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    onResponseOfferInfo(response, o, finish);
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
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", basicAuth);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        jsonObjRequest.setTag(tag);
        mapActivity.queue.add(jsonObjRequest);
    }

//    private void requestSearchCategory(String url, String tag, final String input, final int dep) {
//        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    onResponseCategory(response, input, dep);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Authorization", basicAuth);
//                headers.put("Content-Type", "application/json; charset=utf-8");
//                return headers;
//            }
//        };
//        jsonObjRequest.setTag(tag);
//        mapActivity.queue.add(jsonObjRequest);
//    }

    // response
    private void onResponseSearchMostCommonCategory(JSONObject response, String input) throws JSONException {
        setBestCommonCategory(response);
        if (bestCategory != null) {
            this.searchOffersByCategory(input, 0);
        }
    }

    private void onResponseSearchOffersByCategory(JSONObject response, String input) throws JSONException {
        List<Offer> tempOffers = getOffersListFromJSON(response, input);
        Log.i("HERE", tempOffers.size() + "");
        offers.addAll(tempOffers);
        int offset = response.getJSONObject("pagination").getInt("offset");
//        if (response.getJSONObject("pagination").has("next") && offset < 20) {
//            searchOffersByCategory(input, offset + 20);
//        } else {
//            boolean finish = false;
//            for (int i = 0; i < 10; i++) {
//                if (i == 9) {
//                    finish = true;
//                }
//                this.getOfferInfo(offers.get(i), finish);
//            }
//        }
        for (Offer o : tempOffers) {
            this.getOfferInfo(o, false);
        }
    }

    private void onResponseOfferInfo(JSONObject response, Offer offer, boolean finish) throws JSONException {
        response = response.getJSONArray("component").getJSONObject(0);
        JSONArray results = response.getJSONObject("offers").getJSONArray("offer");
        Log.i("Quantity", results.length() + " offers for: " + response.getString("name"));
//
//        offers.remove(offer);
//        if (finish) {
//            if (!offers.isEmpty()) {
//                int max = offers.size();
//                if (max > 10) {
//                    max = 10;
//                }
//                finish = false;
//                for (int i = 0; i < max; i++) {
//                    if (i == max - 1) {
//                        finish = true;
//                    }
//                    this.getOfferInfo(offers.get(i), finish);
//                }
//            }
//        }

        String dealer, dealerLogo, dealerId;
        double price;

        for (int i = 0; i < results.length(); i++) {
            Offer o = new Offer(offer.getType(), offer.getAvailability(), offer.getTitle(), offer.getClickUrl(), offer.getCategory(), offer.getPhotoId(), offer.getId());
            JSONObject result = results.getJSONObject(i);
            price = result.getDouble("price");
            dealer = result.getString("dealer");
            dealerId = result.getString("id_dealer");
            Shop shop = new Shop(Constants.OFFER_SKAPIEC, dealer, dealerId);
            if (!result.isNull("dealer_logo_url")) {
                dealerLogo = result.getString("dealer_logo_url");
                shop.setLogoUrl(dealerLogo);
            }
            o.setPrice(price);
            o.setProducer(dealer);
            o.setShop(shop);

            if (!mapActivity.shops.contains(shop)) {
                if (o.getPrice() < shop.getMinPrice()) {
                    shop.setMinPrice(o.getPrice());
                }
                mapActivity.shops.add(shop);
                // get locations
                boolean is = false;
                for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
                    if (shop.getName().toLowerCase().equals(Constants.BLACK_LIST_SHOPS[p].toLowerCase())) {
                        is = true;
                    }
                }
                if (!is) {
                    mapActivity.nokautHelper.setShopLocations(shop, false);
                }
            } else {
                for (Shop shop1 : mapActivity.shops) {
                    if (shop1.getName().toLowerCase().equals(shop.getName().toLowerCase())) {
                        if (o.getPrice() < shop1.getMinPrice()) {
                            shop1.setMinPrice(o.getPrice());
                        }
                        o.setShop(shop1);
                    }
                }
            }

            if (!mapActivity.offers.contains(o)) {
                mapActivity.offers.add(o);
            }
            mapActivity.changeFooterInfo();
        }
    }

//    private void onResponseCategory(JSONObject response, String input, int dep) throws JSONException {
//        JSONArray categories = response.getJSONArray("category");
//        String name;
//        for (int i = 0; i < categories.length(); i++) {
//            JSONObject category = categories.getJSONObject(i);
//            name = category.getString("name");
//            if (name.contains(input)) {
//                categoryId = category.getString("id");
//                ;
//                break;
//            }
//        }
//        if (categoryId == null) {
//            if (dep != 29) {
//                this.searchCategory(input, getNextDepartment(dep));
//            }
//        } else {
//            // search offers from category
//            this.searchOffersByCategory("", 0);
//        }
//    }

    // others
    private void setBestCommonCategory(JSONObject response) throws JSONException {
        JSONArray results = response.getJSONObject("components").getJSONArray("component");
        List<Integer> categories = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            String id = jsonObject.getString("id_skapiec");
            String category = jsonObject.getString("link").replace("https://www.skapiec.pl/cat/", "").replaceAll("/comp/" + id, "");
            categories.add(Integer.parseInt(category));
        }
        Log.i("CATEGORIES", categories.toString());
        bestCategory = String.valueOf(mostCommonCategory(categories));
        Log.i("COMMON_CATEGORY", bestCategory);
        if (bestCategory.equals("null")) {
            bestCategory = null;
        }
    }

    private List<Offer> getOffersListFromJSON(JSONObject response, String input) throws JSONException {
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

    @Nullable
    private Integer mostCommonCategory(List<Integer> list) {
        Map<Integer, Integer> map = new HashMap<>();

        for (Integer t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<Integer, Integer> max = null;

        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max != null ? max.getKey() : null;
    }

//    private int getNextDepartment(int dep) {
//        for (int i = 0; i < Constants.DEPARTMENTS_IDS_SKAPIEC.length; i++) {
//            int id = Constants.DEPARTMENTS_IDS_SKAPIEC[i];
//            if (id == dep) {
//                return Constants.DEPARTMENTS_IDS_SKAPIEC[i + 1];
//            }
//        }
//        return 0;
//    }
}