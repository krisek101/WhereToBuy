package simpleapp.wheretobuy.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View myContentsView;
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
        List<Offer> offers = mapActivity.getOffersByLocation(marker.getPosition());
        myContentsView = LayoutInflater.from(mapActivity.getApplicationContext()).inflate(R.layout.marker_info_window, null);
        TextView shopName = (TextView) myContentsView.findViewById(R.id.shop_name);
        TextView price = (TextView) myContentsView.findViewById(R.id.price);

        if (!offers.isEmpty()) {
            Offer sampleOffer = offers.get(0);
            Shop shop = sampleOffer.getShop();
            double minPrice = 10000000, maxPrice = 0;

            if (offers.size() == 1) {
                price.setText("Cena: " + sampleOffer.getPrice() + " zł");
            } else {
                for (Offer offer : offers) {
                    if (offer.getPrice() > maxPrice) maxPrice = offer.getPrice();
                    if (offer.getPrice() < minPrice) minPrice = offer.getPrice();
                }
                price.setText("Ceny: " + minPrice + " - " + maxPrice + " zł");
            }
            shopName.setText(shop.getName());
        } else if (marker.getPosition().equals(mapActivity.userLocation)) {
            shopName.setText("Moja lokalizacja");
            price.setVisibility(View.GONE);
        }

        return myContentsView;
    }
}