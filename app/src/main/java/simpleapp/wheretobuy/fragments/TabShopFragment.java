package simpleapp.wheretobuy.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.models.Shop;

public class TabShopFragment extends Fragment {

    Shop shop;

    public TabShopFragment() {
    }

    public static TabShopFragment newInstance(Shop shop){
        TabShopFragment f = new TabShopFragment();
        f.shop = shop;
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_about_shop, container, false);
        TextView textView = (TextView) view.findViewById(R.id.shop_tel);
        textView.setText(shop.getName());
        return view;
    }
}