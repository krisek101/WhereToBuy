package simpleapp.wheretobuy.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.OffersAdapter;
import simpleapp.wheretobuy.models.Offer;

public class TabOffersInfoWindowFragment extends Fragment {

    List<Offer> offersByPosition;
    MapActivity mapActivity;

    public TabOffersInfoWindowFragment() {
    }

    public static TabOffersInfoWindowFragment newInstance(List<Offer> offersByPosition, MapActivity mapActivity){
        TabOffersInfoWindowFragment f = new TabOffersInfoWindowFragment();
        f.offersByPosition = offersByPosition;
        f.mapActivity = mapActivity;
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_offers, container, false);
        ListView shopOffers = (ListView) view.findViewById(R.id.footer_offers);

        // Offers list
        Collections.sort(offersByPosition);
        OffersAdapter offersAdapter = new OffersAdapter(mapActivity, R.layout.offer, offersByPosition, "offer_map");
        shopOffers.setAdapter(offersAdapter);
        return view;
    }
}