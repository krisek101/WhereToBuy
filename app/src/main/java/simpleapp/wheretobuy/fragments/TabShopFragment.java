package simpleapp.wheretobuy.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.ReviewsAdapter;
import simpleapp.wheretobuy.models.ShopLocation;

public class TabShopFragment extends Fragment {

    ShopLocation shopLocation;
    MapActivity mapActivity;

    public TabShopFragment() {
    }

    public static TabShopFragment newInstance(ShopLocation shopLocation, MapActivity mapActivity) {
        TabShopFragment f = new TabShopFragment();
        f.shopLocation = shopLocation;
        f.mapActivity = mapActivity;
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_about_shop, container, false);

        if (shopLocation.getPhoneNumber() == null || shopLocation.getWebsite() == null || shopLocation.getOpenHours() == null || shopLocation.getReviews() == null) {
            mapActivity.googleHelper.updateShopLocations(shopLocation.getId(), shopLocation);
        }

        // UI
        final TextView phoneNumber = (TextView) view.findViewById(R.id.shop_tel);
        final TextView website = (TextView) view.findViewById(R.id.shop_url);
        final Spinner hours = (Spinner) view.findViewById(R.id.shop_hours);
        final ListView reviewsList = (ListView) view.findViewById(R.id.shop_reviews);

        // Setters
        final Handler h = new Handler();
        final int delay = 500;
        final Runnable[] runnable = new Runnable[1];
        final boolean[] already = {true};
        h.postDelayed(new Runnable() {
            public void run() {
                if (already[0]) {
                    if (shopLocation.getPhoneNumber() != null && shopLocation.getWebsite() != null && shopLocation.getOpenHours() != null && shopLocation.getReviews() != null) {
                        already[0] = false;
                        if (!shopLocation.getPhoneNumber().isEmpty()) {
                            phoneNumber.setText(shopLocation.getPhoneNumber());
                            phoneNumber.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:" + shopLocation.getPhoneNumber()));
                                    startActivity(callIntent);
                                }
                            });
                        } else {
                            phoneNumber.setVisibility(View.GONE);
                        }
                        if (!shopLocation.getWebsite().isEmpty()) {
                            website.setText(shopLocation.getWebsite());
                            website.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent callIntent = new Intent(Intent.ACTION_VIEW);
                                    callIntent.setData(Uri.parse(shopLocation.getWebsite()));
                                    startActivity(callIntent);
                                }
                            });
                        } else {
                            website.setVisibility(View.GONE);
                        }
                        if (!shopLocation.getOpenHours().isEmpty()) {
                            Calendar cal = Calendar.getInstance();
                            int day = cal.get(Calendar.DAY_OF_WEEK);
                            int today = 0;
                            String[] array = new String[shopLocation.getOpenHours().size()];
                            shopLocation.getOpenHours().toArray(array);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(mapActivity, android.R.layout.simple_spinner_dropdown_item, array);
                            hours.setAdapter(adapter);
                            switch(day){
                                case Calendar.SUNDAY:
                                    today = 6;
                                    break;
                                case Calendar.MONDAY:
                                    today = 0;
                                    break;
                                case Calendar.TUESDAY:
                                    today = 1;
                                    break;
                                case Calendar.WEDNESDAY:
                                    today = 2;
                                    break;
                                case Calendar.THURSDAY:
                                    today = 3;
                                    break;
                                case Calendar.FRIDAY:
                                    today = 4;
                                    break;
                                case Calendar.SATURDAY:
                                    today = 5;
                                    break;

                            }
                            hours.setSelection(today);
                        } else {
                            hours.setVisibility(View.GONE);
                        }
                        if (shopLocation.getReviews() != null) {
                            final List<JSONObject> reviews = new ArrayList<>();
                            for (int i = 0; i < shopLocation.getReviews().length(); i++) {
                                try {
                                    reviews.add((JSONObject) shopLocation.getReviews().get(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            final ReviewsAdapter reviewsAdapter = new ReviewsAdapter(mapActivity, R.layout.review_item, reviews);
                            reviewsList.setAdapter(reviewsAdapter);
                        } else {
                            reviewsList.setVisibility(View.GONE);
                        }

                    }
                } else {
                    h.removeCallbacks(runnable[0]);
                }
                runnable[0] = this;
                h.postDelayed(runnable[0], delay);
            }
        }, delay);

        return view;
    }
}