package simpleapp.wheretobuy.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.helpers.PhotoHelper;
import simpleapp.wheretobuy.models.Offer;

public class OffersAdapter extends ArrayAdapter<Offer> {

    private List<Offer> offers;
    private Context context;
    private String type;

    public OffersAdapter(@NonNull Context context, @LayoutRes int resource, List<Offer> offers, String type) {
        super(context, resource, offers);
        this.offers = offers;
        this.context = context;
        this.type = type;
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
        TextView shopDistance = (TextView) convertView.findViewById(R.id.distance);
        TextView shopName = (TextView) convertView.findViewById(R.id.shop_name);

        title.setText(offer.getTitle());
        price.setText(UsefulFunctions.getPriceFormat(offer.getPrice()));
        switch (offer.getAvailability()) {
            case 0:
                availability.setText(R.string.available);
                availability.setTextColor(context.getResources().getColor(R.color.green));
                break;
            case 1:
                availability.setText(R.string.available_in_week);
                availability.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 2:
                availability.setText(R.string.available_more_week);
                availability.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 3:
                availability.setText(R.string.available_wish);
                availability.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            default:
                availability.setText(R.string.check_in_shop);
                availability.setTextColor(context.getResources().getColor(R.color.orange));
                break;
        }
        String offerUrl;
        if (offer.getType().equals(Constants.OFFER_NOKAUT)) {
            PhotoHelper photoHelper = new PhotoHelper(offer.getPhotoId(), offer.getTitle(), "130x130");
            offerUrl = photoHelper.getPhotoUrl();
            Picasso.with(context).load(offerUrl).into(photo);
        } else if (offer.getType().equals(Constants.OFFER_SKAPIEC)) {
            if (offer.getPhotoId() != null) {
                offerUrl = offer.getPhotoId();
                Picasso.with(context).load(offerUrl).into(photo);
            }
        }
        switch (type) {
            case "offer_footer":
                if (offer.getShop().getBestDistance() != -1) {
                    shopDistance.setVisibility(View.VISIBLE);
                    String distance = UsefulFunctions.getDistanceKilometersFormat(offer.getShop().getBestDistance());
                    shopDistance.setText(distance);
                } else {
                    shopDistance.setVisibility(View.GONE);
                }
                shopName.setText(offer.getShop().getName());
                break;
            default:
                shopDistance.setVisibility(View.GONE);
                shopName.setVisibility(View.GONE);
                break;
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                if (offer.getType().equals(Constants.OFFER_NOKAUT)) {
                    callIntent.setData(Uri.parse("http://nokaut.click" + offer.getClickUrl()));
                } else if (offer.getType().equals(Constants.OFFER_SKAPIEC)) {
                    callIntent.setData(Uri.parse(offer.getClickUrl() + "#from107726"));
                }
                context.startActivity(callIntent);
            }
        });
        return convertView;
    }
}