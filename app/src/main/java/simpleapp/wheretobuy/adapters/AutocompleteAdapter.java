package simpleapp.wheretobuy.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.models.AutoCompleteResult;

public class AutocompleteAdapter extends ArrayAdapter<AutoCompleteResult> {

    private MapActivity mapActivity;
    private Context context;
    private List<AutoCompleteResult> autocompleteProducts;

    public AutocompleteAdapter(@LayoutRes int resource, List<AutoCompleteResult> autocompleteProducts, MapActivity mapActivity) {
        super(mapActivity.getBaseContext(), resource, autocompleteProducts);
        this.context = mapActivity.getBaseContext();
        this.mapActivity = mapActivity;
        this.autocompleteProducts = autocompleteProducts;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.autocomplete_item, parent, false);
        }

        final AutoCompleteResult result = autocompleteProducts.get(position);

        // UI
        Log.i("RESULT NAME", result.getName());
        TextView name = (TextView) convertView.findViewById(R.id.autocomplete_name);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.autocomplete_product);
        name.setText(result.getName());

        // listener
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = autocompleteProducts.size();

                // clear
                mapActivity.offers.clear();
                mapActivity.shops.clear();
                mapActivity.clearShopMarkers();

                // make query
                if (result.getId().equals("all")) {
                    if (size == 21) {
                        mapActivity.requestHelper.setTag(Constants.TAG_MORE_PRODUCTS);
                        mapActivity.requestHelper.setMoreProductsUrl(result.getName(), 20);
                        mapActivity.requestHelper.doRequest(result.getName());
                    } else {
                        for (AutoCompleteResult r : autocompleteProducts) {
                            if (!r.getId().equals("all")) {
                                mapActivity.requestHelper.setTag(Constants.TAG_RESULT_DETAILS);
                                mapActivity.requestHelper.setResultDetailsUrl(r);
                                mapActivity.requestHelper.doRequest("");
                            }
                        }
                    }
                } else {
                    mapActivity.requestHelper.setTag(Constants.TAG_RESULT_DETAILS);
                    mapActivity.requestHelper.setResultDetailsUrl(result);
                    mapActivity.requestHelper.doRequest("");
                }

                // search field
                mapActivity.searchText.setText(result.getName());
                mapActivity.searchText.dismissDropDown();
                mapActivity.hideKeyboard();

                // loading start/stop
                mapActivity.setLoading(true);
                if (result.getId().equals("all")) {
                    if (size == 1) {
                        mapActivity.setLoading(false);
                    }
                }
            }
        });

        return convertView;
    }
}