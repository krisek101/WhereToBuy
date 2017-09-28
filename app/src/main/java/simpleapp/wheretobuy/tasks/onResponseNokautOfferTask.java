package simpleapp.wheretobuy.tasks;

import android.os.AsyncTask;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class onResponseNokautOfferTask extends AsyncTask<Void, Void, List<Shop>> {

    MapActivity mapActivity;
    JSONObject response;
    String tag;

    public onResponseNokautOfferTask(MapActivity mapActivity, JSONObject response, String tag) {
        this.mapActivity = mapActivity;
        this.response = response;
        this.tag = tag;
    }

    @Override
    protected List<Shop> doInBackground(Void... voids) {
        List<Shop> uniqueShops = new ArrayList<>();
        try {
            if (response.has("offers")) {
                JSONArray ja = response.getJSONArray("offers");
                if (ja.length() != 0) {
                    Shop shopModel;
                    Offer offerModel;
                    for (int i = 0; i < ja.length(); i++) {
                        // build models
                        offerModel = buildOfferModel(ja.getJSONObject(i));
                        shopModel = buildShopModel(ja.getJSONObject(i).getJSONObject("shop"));

                        // Assign shop to offer
                        if (!mapActivity.shops.contains(shopModel)) {
                            // new shop - add it
                            if (offerModel.getPrice() < shopModel.getMinPrice()) {
                                shopModel.setMinPrice(offerModel.getPrice());
                            }
                            if (offerModel.getPrice() > shopModel.getMaxPrice()) {
                                shopModel.setMaxPrice(offerModel.getPrice());
                            }
                            shopModel.setTotalCountOffers(1);
                            mapActivity.shops.add(shopModel);
                            offerModel.setShop(shopModel);
                            mapActivity.offers.add(offerModel);

                            // get locations
                            boolean is = false;
                            for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
                                if (shopModel.getName().toLowerCase().equals(Constants.BLACK_LIST_SHOPS[p].toLowerCase())) {
                                    is = true;
                                }
                            }
                            if (!is) {
                                uniqueShops.add(shopModel);
                            }
                        } else {
                            // shop already exists
                            if (!mapActivity.offers.contains(offerModel)) {
                                for (Shop shop1 : mapActivity.shops) {
                                    if (shop1.equals(shopModel)) {
                                        if (offerModel.getPrice() < shop1.getMinPrice()) {
                                            shop1.setMinPrice(offerModel.getPrice());
                                        }
                                        if (offerModel.getPrice() > shop1.getMaxPrice()) {
                                            shop1.setMaxPrice(offerModel.getPrice());
                                        }
                                        shop1.setTotalCountOffers(shop1.getTotalCountOffers() + 1);
                                        offerModel.setShop(shop1);
                                        mapActivity.offers.add(offerModel);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uniqueShops;
    }

    @Override
    protected void onPostExecute(List<Shop> uniqueShops) {
        super.onPostExecute(uniqueShops);
        mapActivity.loadingHelper.changeLoader(-1, tag);
        for (Shop shop : uniqueShops) {
            mapActivity.googleHelper.searchNearbyShops(shop, Constants.SEARCH_RADIUS);
        }
    }

    private Offer buildOfferModel(JSONObject offer) throws JSONException {
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
        return new Offer(Constants.OFFER_NOKAUT, offerAvailability, offerPrice, offerTitle, offerCategory, offerDesc, offerClickUrl, offerProducer, offerPhotoId);
    }

    private Shop buildShopModel(JSONObject shop) throws JSONException {
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
        return new Shop(Constants.OFFER_NOKAUT, shopName, shopUrl, shopLogoUrl, shopId);
    }

}