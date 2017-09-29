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

    private MapActivity mapActivity;
    private JSONObject response;
    private Offer offer;
    private String tag;
    private List<Offer> offers = new ArrayList<>();

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
                // JSON to POJO
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

                // LONG TIME OPERATIONS
                if (!mapActivity.shops.contains(shop)) {
                    // create new shop
                    if (o.getPrice() < shop.getMinPrice()) {
                        shop.setMinPrice(o.getPrice());
                    }
                    if (o.getPrice() > shop.getMaxPrice()) {
                        shop.setMaxPrice(o.getPrice());
                    }
                    shop.setTotalCountOffers(1);
                    mapActivity.shops.add(shop);
                    o.setShop(shop);
                    offers.add(o);
                    // get locations
                    boolean is = false;
                    for (int p = 0; p < Constants.ONLINE_SHOPS.length; p++) {
                        if (shop.getName().toLowerCase().equals(Constants.ONLINE_SHOPS[p].toLowerCase())) {
                            is = true;
                        }
                    }
                    if (!is) {
                        uniqueShops.add(shop);
                    }
                } else {
                    // shop already exists
                    if (!mapActivity.offers.contains(o)) {
                        for (Shop shop1 : mapActivity.shops) {
                            if (shop1.equals(shop)) {
                                shop1.setTotalCountOffers(shop1.getTotalCountOffers() + 1);
                                offers.add(o);
                                if (o.getPrice() < shop1.getMinPrice()) {
                                    shop1.setMinPrice(o.getPrice());
                                }
                                if (o.getPrice() > shop1.getMaxPrice()) {
                                    shop1.setMaxPrice(o.getPrice());
                                }
                                o.setShop(shop1);
                                break;
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
        for (Shop shop : uniqueShops) {
            mapActivity.googleHelper.searchNearbyShops(shop, Constants.SEARCH_RADIUS);
        }
//        endTime = System.nanoTime();
//        Log.i("SKAPIEC ASYNCTASK", (endTime - startTime) / 1e6 + "");
        mapActivity.offers.addAll(offers);
        mapActivity.offersAdapter.notifyDataSetChanged();
        mapActivity.loadingHelper.changeLoader(-1, tag);
    }
}