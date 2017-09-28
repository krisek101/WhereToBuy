package simpleapp.wheretobuy.tasks;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class onResponseSkapiecOfferTask extends AsyncTask<Void, Void, List<Shop>> {

    MapActivity mapActivity;
    JSONObject response;
    Offer offer;
    String tag;

    public onResponseSkapiecOfferTask(MapActivity mapActivity, JSONObject response, Offer offer, String tag) {
        this.mapActivity = mapActivity;
        this.response = response;
        this.offer = offer;
        this.tag = tag;
    }

    @Override
    protected List<Shop> doInBackground(Void... voids) {
        List<Shop> uniqueShops = new ArrayList<>();
        try {
            response = response.getJSONArray("component").getJSONObject(0);
            JSONArray results = response.getJSONObject("offers").getJSONArray("offer");
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


                Shop tempShop = null;
                for (Shop s : mapActivity.shops) {
                    if (s.equals(shop)) {
                        tempShop = s;
                    }
                }

                if (tempShop == null) {
                    if (o.getPrice() < shop.getMinPrice()) {
                        shop.setMinPrice(o.getPrice());
                    }
                    if (o.getPrice() > shop.getMaxPrice()) {
                        shop.setMaxPrice(o.getPrice());
                    }
                    o.getShop().setTotalCountOffers(1);
                    mapActivity.shops.add(shop);
                    mapActivity.offers.add(o);
                    // get locations
                    boolean is = false;
                    for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
                        if (shop.getName().toLowerCase().equals(Constants.BLACK_LIST_SHOPS[p].toLowerCase())) {
                            is = true;
                        }
                    }
                    if (!is) {
                        uniqueShops.add(shop);
                    }
                } else {
                    List<Offer> offersFromShop = mapActivity.getOffersByShop(tempShop);
                    if (!offersFromShop.contains(o)) {
                        tempShop.setTotalCountOffers(tempShop.getTotalCountOffers() + 1);
                        mapActivity.offers.add(o);
                    }
                    if (o.getPrice() < tempShop.getMinPrice()) {
                        tempShop.setMinPrice(o.getPrice());
                    }
                    if (o.getPrice() > tempShop.getMaxPrice()) {
                        tempShop.setMaxPrice(o.getPrice());
                    }
                    o.setShop(tempShop);
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
        for (Shop shop : uniqueShops) {
            mapActivity.googleHelper.searchNearbyShops(shop, Constants.SEARCH_RADIUS);
        }
        mapActivity.loadingHelper.changeLoader(-1, tag);
    }
}