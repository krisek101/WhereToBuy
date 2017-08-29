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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;

public class AutocompleteAddressesAdapter extends ArrayAdapter<String> {

    private MapActivity mapActivity;
    private Context context;
    private List<String> ids;
    private List<String> results;

    public AutocompleteAddressesAdapter(@LayoutRes int resource, Map<String, String> autoCompleteResults, MapActivity mapActivity) {
        super(mapActivity.getBaseContext(), resource, new ArrayList<>(autoCompleteResults.keySet()));
        this.context = mapActivity.getBaseContext();
        this.mapActivity = mapActivity;
        this.ids = new ArrayList<>(autoCompleteResults.keySet());
        this.results = new ArrayList<>(autoCompleteResults.values());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.autocomplete_item, parent, false);
        }

        final String result = results.get(position);
        final String id = ids.get(position);

        // UI
        TextView name = (TextView) convertView.findViewById(R.id.autocomplete_name);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.autocomplete_product);
        name.setText(result);

        // listener
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set new search position
                mapActivity.googleHelper.changeUserLocation(id);

                mapActivity.searchLocation.setText(result);
                mapActivity.searchLocation.dismissDropDown();
                mapActivity.hideKeyboard();
            }
        });

        return convertView;
    }
}