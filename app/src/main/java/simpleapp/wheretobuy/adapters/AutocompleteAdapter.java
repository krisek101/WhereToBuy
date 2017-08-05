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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.RequestToQueue;
import simpleapp.wheretobuy.models.Shop;

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

        Log.i("PRODUCT NAME", result.getName());
        TextView name = (TextView) convertView.findViewById(R.id.autocomplete_name);
        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.autocomplete_product);

        name.setText(result.getName());

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapActivity.mMap.clear();
                mapActivity.offers.clear();
                Marker m = mapActivity.mMap.addMarker(new MarkerOptions().position(mapActivity.userLocation).title("Moja lokalizacja"));
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mapActivity.searchText.setText(result.getName());
                mapActivity.searchText.dismissDropDown();
                RequestToQueue requestToQueue = new RequestToQueue(Constants.TAG_RESULT_DETAILS, mapActivity);
                requestToQueue.setResultDetailsUrl(result);
                requestToQueue.doRequest();
            }
        });

        return convertView;
    }
}