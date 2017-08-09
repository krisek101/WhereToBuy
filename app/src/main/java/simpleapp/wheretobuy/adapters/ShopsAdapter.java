package simpleapp.wheretobuy.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;

public class ShopsAdapter extends ArrayAdapter<Shop>{

    private List<Shop> shops;
    private MapActivity mapActivity;
    private Context context;

    public ShopsAdapter(@NonNull MapActivity mapActivity, @LayoutRes int resource, List<Shop> shops) {
        super(mapActivity.getApplicationContext(), resource, shops);
        this.shops = shops;
        this.mapActivity = mapActivity;
        this.context = mapActivity.getApplicationContext();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.shop, null);
        }

        final Shop shop = shops.get(position);
        final List<Offer> offersFromShop = getOffersFromShop(shop);
        Collections.sort(offersFromShop);

        // UI
        TextView shopNameText = (TextView) convertView.findViewById(R.id.shop_name);
        TextView priceText = (TextView) convertView.findViewById(R.id.price);
        ImageView photo = (ImageView) convertView.findViewById(R.id.photo);

        // Setters
        String logoUrl = "http://offers.gallery" + shop.getLogoUrl();
        Picasso.with(context).load(logoUrl).into(photo);
        shopNameText.setText(shop.getName());
        priceText.setText("Ceny: " + UsefulFunctions.getPriceFormat(offersFromShop.get(0).getPrice()) + " - " + UsefulFunctions.getPriceFormat(offersFromShop.get(offersFromShop.size()-1).getPrice()));

        // Listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!shop.getLocations().isEmpty()) {
                    mapActivity.showOffersInAlertDialog(offersFromShop, shop.getLocations().get(0));
                }else{
                    mapActivity.showOffersInAlertDialog(offersFromShop, null);
                }
            }
        });
        return convertView;
    }

    private List<Offer> getOffersFromShop(Shop shop){
        List<Offer> offers = new ArrayList<>();
        for(Offer offer : mapActivity.offers){
            if(offer.getShop().getName().equals(shop.getName())){
                offers.add(offer);
            }
        }
        return offers;
    }
}