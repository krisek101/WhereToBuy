package simpleapp.wheretobuy.models;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.adapters.SectionsPageAdapter;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.fragments.TabOffersInfoWindowFragment;
import simpleapp.wheretobuy.fragments.TabShopFragment;
import simpleapp.wheretobuy.tasks.GeocodeTask;

public class CustomDialog extends DialogFragment implements View.OnClickListener {

    List<Offer> offersByPosition;
    ShopLocation shopLocation;
    MapActivity mapActivity;

    public CustomDialog() {
    }

    public static CustomDialog newInstance(List<Offer> offersByPosition, ShopLocation shopLocation, MapActivity mapActivity) {
        CustomDialog f = new CustomDialog();
        f.offersByPosition = offersByPosition;
        f.shopLocation = shopLocation;
        f.mapActivity = mapActivity;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init
        View view = inflater.inflate(R.layout.marker_window_shop, container);
        Shop shop = offersByPosition.get(0).getShop();

        ViewPager mInfoWindowViewPager = (ViewPager) view.findViewById(R.id.info_window_pager);
        SectionsPageAdapter adapter = new SectionsPageAdapter(getChildFragmentManager());

        TabLayout tabInfoWindowLayout = (TabLayout) view.findViewById(R.id.info_window_pager_tabs);
        tabInfoWindowLayout.setupWithViewPager(mInfoWindowViewPager);

        TabOffersInfoWindowFragment tabOffersInfoWindowFragment = TabOffersInfoWindowFragment.newInstance(offersByPosition, mapActivity);
        adapter.addFragment(tabOffersInfoWindowFragment, "Oferty");
        if (shopLocation != null) {
            TabShopFragment tabShopFragment = TabShopFragment.newInstance(shopLocation, mapActivity);
            adapter.addFragment(tabShopFragment, "O sklepie");
        }
        mInfoWindowViewPager.setAdapter(adapter);

        // UI
        ImageView exit = (ImageView) view.findViewById(R.id.info_window_exit);
        TextView shopName = (TextView) view.findViewById(R.id.distance);
        TextView shopAddress = (TextView) view.findViewById(R.id.shop_address);
        ImageView shopLogo = (ImageView) view.findViewById(R.id.shop_logo);

        // Setters
        exit.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        exit.setOnClickListener(this);
        shopName.setText(shop.getName());
        if (shopLocation != null) {
            if (shopLocation.getAddress().isEmpty()) {
                new GeocodeTask(mapActivity, shopLocation.getLocation(), shopAddress).execute();
            } else {
                shopAddress.setText(shopLocation.getAddress());
            }
        } else {
            shopAddress.setVisibility(View.GONE);
        }
        if(!shop.getLogoUrl().isEmpty()) {
            String logoUrl = "";
            if (shop.getType().equals(Constants.OFFER_SKAPIEC)) {
                logoUrl = shop.getLogoUrl();
            } else if (shop.getType().equals(Constants.OFFER_NOKAUT)) {
                logoUrl = "http://offers.gallery" + shop.getLogoUrl();
            }
            Picasso.with(mapActivity).load(logoUrl).into(shopLogo);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info_window_exit:
                getDialog().dismiss();
                break;
        }
    }
}