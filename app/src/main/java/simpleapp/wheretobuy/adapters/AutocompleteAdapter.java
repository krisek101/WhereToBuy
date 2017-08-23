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
                mapActivity.queryOffers(result);
            }
        });

        return convertView;
    }
}