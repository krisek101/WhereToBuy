package simpleapp.wheretobuy.helpers;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;
import simpleapp.wheretobuy.tasks.onResponseGoogleNearbyShops;
import simpleapp.wheretobuy.tasks.onResponseNokautOfferTask;
import simpleapp.wheretobuy.tasks.onResponseSkapiecOfferTask;

public class LoadingHelper {

    private MapActivity mapActivity;
    private Map<String, Integer> loaders = new HashMap<>();
    public boolean isLoading = false;

    public LoadingHelper(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
    }

    public void changeLoader(int value, String tag) {
        boolean exists = false;
        if (!loaders.keySet().isEmpty()) {
            for (String loader : loaders.keySet()) {
                if (loader.equals(tag)) {
                    exists = true;
                    loaders.put(tag, loaders.get(tag) + value);
                    break;
                }
            }
        }
        if (!exists) {
            loaders.put(tag, value);
        }
        checkAllLoaders();
    }

    private void checkAllLoaders() {
        int zeroElements = 0;
        for (String tag : loaders.keySet()) {
            int value = loaders.get(tag);
            if (value == 0) {
                zeroElements++;
            }
        }

        if (loaders.keySet().size() == zeroElements && isLoading) {
            animateCamera();
            stopSearching();
            isLoading = false;
        } else {
            isLoading = true;
        }
        mapActivity.changeFooterInfo();
    }

    public void animateCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean has = false;
        List<Shop> tempShops = new ArrayList<>(mapActivity.shops);
        for (Shop s : tempShops) {
            if (!s.getLocations().isEmpty()) {
                for (ShopLocation shopLocation : s.getLocations()) {
                    if (shopLocation.getMarker() != null) {
                        builder.include(shopLocation.getMarker().getPosition());
                        has = true;
                    }
                }
            }
        }
        if (mapActivity.userLocation != null) {
            builder.include(mapActivity.userLocation);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 140);
        if (has) {
            mapActivity.mMap.animateCamera(cu);
        }
    }

    public void stopSearching() {
        // cancel all requests
        for (String loader : loaders.keySet()) {
            mapActivity.queue.cancelAll(loader);
        }
        loaders.clear();

        // cancel all AsyncTasks
        for (onResponseSkapiecOfferTask task : mapActivity.skapiecOffersTasks) {
            task.cancel(true);
        }
        for (onResponseNokautOfferTask task : mapActivity.nokautOffersTasks) {
            task.cancel(true);
        }
        for (onResponseGoogleNearbyShops task : mapActivity.nearbyShopsTasks) {
            task.cancel(true);
        }

        mapActivity.skapiecOffersTasks.clear();
        mapActivity.nokautOffersTasks.clear();
        mapActivity.nearbyShopsTasks.clear();

        isLoading = false;
    }

    public void stopLoadingByTag(String tag) {
        loaders.put(tag, 0);
    }

}