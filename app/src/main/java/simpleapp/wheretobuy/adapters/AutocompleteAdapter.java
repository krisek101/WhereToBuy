package simpleapp.wheretobuy.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private List<AutoCompleteResult> autoCompleteResults;

    public AutocompleteAdapter(@LayoutRes int resource, List<AutoCompleteResult> autoCompleteResults, MapActivity mapActivity) {
        super(mapActivity.getBaseContext(), resource, autoCompleteResults);
        this.context = mapActivity.getBaseContext();
        this.mapActivity = mapActivity;
        this.autoCompleteResults = autoCompleteResults;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.autocomplete_item, parent, false);
        }

        final AutoCompleteResult result = autoCompleteResults.get(position);

        // UI
        //Log.i("RESULT NAME", result.getName());
        TextView name = (TextView) convertView.findViewById(R.id.autocomplete_name);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.autocomplete_product);
        name.setText(result.getName());

        // listener
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mapActivity.userLocation == null){
                    mapActivity.checkUsersSettingGPS();
                }
                // clear
                mapActivity.offers.clear();
                mapActivity.clearShopMarkers();
                mapActivity.shops.clear();

                // make query
                if (result.getId().equals("all")) {
                    if (autoCompleteResults.size() >= 18) {
                        mapActivity.requestHelper.setTag(Constants.TAG_MORE_PRODUCTS);
                        mapActivity.requestHelper.setMoreProductsUrl(result.getName(), 20);
                        mapActivity.requestHelper.doRequest(result.getName());
                    } else {
                        for (int i = 0; i < mapActivity.autoCompleteResults.size() - 1; i++) {
                            if (mapActivity.autoCompleteResults.get(i).getId().equals("all")) {
                                mapActivity.autoCompleteResults.remove(i);
                                autoCompleteResults.remove(i);
                            }
                        }
                        for (AutoCompleteResult r : autoCompleteResults) {
                            mapActivity.requestHelper.setTag(Constants.TAG_OFFERS);
                            mapActivity.requestHelper.setOffersUrl(r);
                            mapActivity.requestHelper.doRequest("");
                        }
                    }
                } else {
                    AutoCompleteResult r;
                    r = result;
                    mapActivity.autoCompleteResults.clear();
                    mapActivity.autoCompleteResults.add(r);
                    mapActivity.requestHelper.setTag(Constants.TAG_OFFERS);
                    mapActivity.requestHelper.setOffersUrl(result);
                    mapActivity.requestHelper.doRequest("");
                }

                // search field
                mapActivity.loading = true;
                mapActivity.searchText.setText(result.getName());
                mapActivity.searchText.dismissDropDown();
                mapActivity.hideKeyboard();

                // loading start/stop
                mapActivity.setLoading(true);
            }
        });

        return convertView;
    }
}