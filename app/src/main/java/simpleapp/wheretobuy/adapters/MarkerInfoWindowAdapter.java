package simpleapp.wheretobuy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private MapActivity mapActivity;

    public MarkerInfoWindowAdapter(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // UI
        View myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window, null);
        TextView shopName = (TextView) myContentsView.findViewById(R.id.distance);
        TextView price = (TextView) myContentsView.findViewById(R.id.price);

        if (marker.getPosition().equals(mapActivity.userLocation)) {
            shopName.setText(mapActivity.getString(R.string.my_location));
            price.setVisibility(View.GONE);
        } else {
            ShopLocation shopLocation = mapActivity.getShopLocationByPosition(marker.getPosition());
            Shop shop = mapActivity.getShopByShopLocation(shopLocation);
            List<Offer> offers = mapActivity.getOffersByShop(shop);

            if (!offers.isEmpty()) {
                Offer sampleOffer = offers.get(0);
                double minPrice = 10000000, maxPrice = 0;

                if (offers.size() == 1) {
                    price.setText(UsefulFunctions.getPriceFormat(sampleOffer.getPrice()));
                } else {
                    for (Offer offer : offers) {
                        if (offer.getPrice() > maxPrice) maxPrice = offer.getPrice();
                        if (offer.getPrice() < minPrice) minPrice = offer.getPrice();
                    }
                    price.setText(UsefulFunctions.getPriceFormat(minPrice) + " - " + UsefulFunctions.getPriceFormat(maxPrice));
                }
                shopName.setText(shop.getName());
            }
        }
        return myContentsView;
    }
}