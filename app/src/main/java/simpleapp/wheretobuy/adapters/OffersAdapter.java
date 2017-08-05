package simpleapp.wheretobuy.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.helpers.PhotoHelper;
import simpleapp.wheretobuy.models.Offer;

public class OffersAdapter extends ArrayAdapter<Offer>{

    private List<Offer> offers;
    private Context context;

    public OffersAdapter(@NonNull Context context, @LayoutRes int resource, List<Offer> offers) {
        super(context, resource, offers);
        this.offers = offers;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.offer, null);
        }

        final Offer offer = offers.get(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView price = (TextView) convertView.findViewById(R.id.price);
        ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
        TextView availability = (TextView) convertView.findViewById(R.id.availability);

        title.setText(offer.getTitle());
        price.setText("Cena: " + offer.getPrice() + " zł");
        if(offer.getAvailability() == 0) {
            availability.setText("Dostępny");
            availability.setTextColor(Color.GREEN);
        }
        PhotoHelper photoHelper = new PhotoHelper(offer.getPhotoId(), offer.getTitle(), "130x130");
        String offerUrl = photoHelper.getPhotoUrl();
        Picasso.with(context).load(offerUrl).into(photo);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("http://nokaut.click" + offer.getClickUrl()));
                context.startActivity(callIntent);
            }
        });
        return convertView;
    }
}