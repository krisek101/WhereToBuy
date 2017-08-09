package simpleapp.wheretobuy.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.adapters.MarkerInfoWindowAdapter;
import simpleapp.wheretobuy.adapters.OffersAdapter;
import simpleapp.wheretobuy.adapters.ShopsAdapter;
import simpleapp.wheretobuy.constants.ClearableAutoCompleteTextView;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.helpers.ListenerHelper;
import simpleapp.wheretobuy.helpers.PhotoHelper;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.helpers.RequestHelper;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.tasks.GeocoderTask;

import static simpleapp.wheretobuy.constants.Constants.SPEECH_REQUEST_CODE;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Location and map
    public GoogleMap mMap;
    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    public LatLng userLocation;
    private Marker userLocationMarker;

    // UI
    public ClearableAutoCompleteTextView searchText;
    public RelativeLayout footer;
    public boolean footerOpened = false;
    public RelativeLayout footerSlider;
    public float footerTop;

    // Collections
    public List<AutoCompleteResult> autoCompleteResults = new ArrayList<>();
    public List<Offer> offers = new ArrayList<>();
    public List<Shop> shops = new ArrayList<>();

    // Others
    public RequestQueue queue;
    public Offer bestOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        prepareGoogleMap();
        requestPermissions();
        preLocation();
        initUI();
        setListeners();
    }

    // initial functions
    private void initUI() {
        searchText = (ClearableAutoCompleteTextView) findViewById(R.id.search_text);
        searchText.setClearButton(ResourcesCompat.getDrawable(getResources(), R.drawable.clear, null), false);
        queue = Volley.newRequestQueue(this);
        ((FloatingActionButton) findViewById(R.id.getMyLocationButton)).setImageResource(R.drawable.ic_my_location_white_24dp);
        footer = (RelativeLayout) findViewById(R.id.footer);
        footerTop = footer.getY();
        footerSlider = (RelativeLayout) findViewById(R.id.footer_content);
        footerSlider.setY(UsefulFunctions.getScreenHeight(this) - UsefulFunctions.getStatusBarHeight(this));
        footerSlider.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, UsefulFunctions.getScreenHeight(this) - footer.getLayoutParams().height - UsefulFunctions.getStatusBarHeight(this)));
    }

    public void setLoading(boolean load){
        if(load) {
            try {
                GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.loading);
                searchText.setLoadingGif(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            searchText.setClearButton(ResourcesCompat.getDrawable(getResources(), R.drawable.clear, null), true);
        }
    }

    private void setListeners() {
        ListenerHelper listenerHelper = new ListenerHelper(this);
        listenerHelper.setListener(findViewById(R.id.search_mic), "click");
        listenerHelper.setListener(findViewById(R.id.getMyLocationButton), "click");
        listenerHelper.setListener(footer, "touch");
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear
                clearShopsMarkers();
                shops.clear();
                offers.clear();
                autoCompleteResults.clear();
                changeFooterInfo();

                // cancel queue
                if (queue != null) {
                    queue.cancelAll(Constants.TAG_AUTOCOMPLETE);
                }

                // add default element
                autoCompleteResults.add(new AutoCompleteResult("product", s.toString(), "all"));

                // update list from Nokaut API
                RequestHelper categoryRequest = new RequestHelper(Constants.TAG_CATEGORY, MapActivity.this);
                categoryRequest.setCategoryAutocompleteUrl(s.toString());
                categoryRequest.doRequest("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void clearShopsMarkers(){
        for(Shop shop : shops){
            if(shop.getMarkers() != null) {
                for (Marker m : shop.getMarkers()) {
                    m.remove();
                }
            }
        }
    }

    // Permissions and settings
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, Constants.REQUEST_PERMISSIONS_CODE);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PERMISSIONS_SWITCH:
        switch (requestCode) {
            case Constants.REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d("DEBUG", "Permission: " + permissions[i]);
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d("DEBUG", "Any permission denied!");
                            requestPermissions();
                            break PERMISSIONS_SWITCH;
                        }
                    }
                    preLocation();
                    Log.d("DEBUG", "All permissions granted!");
                } else {
                    Log.d("DEBUG", "Request cancelled");
                }
                break;
            default:
                Log.d("DEBUG", "unhandled permission");
                break;
        }
    }

    public void checkUsersSettingGPS() {
        if (UsefulFunctions.isOnline(this)) {
            if (mGoogleApiClient != null) {
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult result2) {
                        final Status status = result2.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // OK
                                getLocation();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // ASK FOR GPS
                                try {
                                    status.startResolutionForResult(
                                            MapActivity.this,
                                            Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                });
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, Constants.REQUEST_CHECK_PLAY_SERVICES).show();
            }
            return false;
        }
        return true;
    }

    // Location
    private void preLocation() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            Log.e("Google Play Services", "Unavailable");
        }
        checkUsersSettingGPS();
    }

    private void getLocation() {
        mGoogleApiClient.disconnect();
        if (mGoogleApiClient != null && UsefulFunctions.isOnline(this)) {
            mGoogleApiClient.connect();
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void setUserLocationMarker() {
        if (userLocationMarker != null) {
            userLocationMarker.remove();
        }
        if(userLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
            userLocationMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.my_location)));
            userLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("Google map", "Connection success");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS_CODE);
        } else {
            if (UsefulFunctions.isOnline(this) && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSIONS_CODE);
        } else {
            if (UsefulFunctions.isOnline(this) && mMap != null) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    // set lat and lng
                    double latitude = mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    userLocation = new LatLng(latitude, longitude);

                    // save current location
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Latitude", String.valueOf(latitude)).apply();
                    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("Longitude", String.valueOf(longitude)).apply();

                    // set user location
                    setUserLocationMarker();

                    // move Camera
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
                    mGoogleApiClient.disconnect();
                    Log.v("Location changed", "lat:" + latitude + ", lng:" + longitude);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Google map", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Google map", "Connection failed");
    }

    // Google Map
    private void prepareGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!marker.equals(userLocationMarker)) {
                    List<Offer> offersByPosition = getOffersByLocation(marker.getPosition());
                    showOffersInAlertDialog(offersByPosition, marker.getPosition());
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideKeyboard();
            }
        });

        restoreData();
    }

    // Others
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            searchText.setText(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void restoreData() {
        // last location
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this).getString("Longitude", "").isEmpty()) {
            userLocation = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            setUserLocationMarker();
        }
    }

    public void hideKeyboard(){
        View view = MapActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Offers
    public List<Offer> getOffersByLocation(LatLng position) {
        List<Offer> offersByPosition = new ArrayList<>();
        for (Offer o : offers) {
            if(o.getShop().getLocations() != null) {
                if (o.getShop().getLocations().contains(position)) {
                    offersByPosition.add(o);
                }
            }
        }
        return offersByPosition;
    }

    public void showOffersInAlertDialog(List<Offer> offersByPosition, LatLng shopLocation) {
        // alert dialog and inflater
        final AlertDialog.Builder offersInfoWindow = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.offers_list_window, null);
        offersInfoWindow.setView(convertView);
        final AlertDialog ad = offersInfoWindow.show();
        if (ad.getWindow() != null) {
            ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // UI
        ImageView exit = (ImageView) convertView.findViewById(R.id.info_window_exit);
        TextView shopName = (TextView) convertView.findViewById(R.id.shop_name);
        TextView shopAddress = (TextView) convertView.findViewById(R.id.shop_address);
        ListView shopOffers = (ListView) convertView.findViewById(R.id.shop_offers);
        ImageView shopLogo = (ImageView) convertView.findViewById(R.id.shop_logo);
        exit.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // Listeners
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.cancel();
            }
        });

        // Logic
        Shop shop = offersByPosition.get(0).getShop();

        // Setters
        shopName.setText(shop.getName());
        if(shopLocation != null) {
            new GeocoderTask(this, shopLocation, shopAddress).execute();
        }else{
            shopAddress.setVisibility(View.GONE);
        }
        String logoUrl = "http://offers.gallery" + shop.getLogoUrl();
        Picasso.with(this).load(logoUrl).into(shopLogo);

        // Offers list
        Collections.sort(offersByPosition);
        OffersAdapter offersAdapter = new OffersAdapter(this, R.layout.offer, offersByPosition);
        shopOffers.setAdapter(offersAdapter);
    }

    public void changeFooterInfo(){
        if(offers.size() == 0){
            footer.setVisibility(View.GONE);
        }else {
            // logic
            int nearMe, outside = 0;
            double bestPrice = 1000000000;
            for (Offer offer : offers) {
                if (offer.getShop().getLocations() == null || (offer.getShop().getLocations() != null && offer.getShop().getLocations().isEmpty())) {
                    outside++;
                }
                if (offer.getPrice() < bestPrice && offer.getPrice() > 0) {
                    bestPrice = offer.getPrice();
                    bestOffer = offer;
                }
            }
            nearMe = offers.size() - outside;

            // UI
            TextView nearMeText = (TextView) findViewById(R.id.near_me_text);
            TextView bestPriceText = (TextView) findViewById(R.id.best_price_text);
            TextView outsideText = (TextView) findViewById(R.id.outside_text);
            ListView listView = (ListView) findViewById(R.id.footer_shops);
            bestPriceText.setTypeface(null, Typeface.BOLD);

            // Setters
            nearMeText.setText(String.valueOf(nearMe));
            bestPriceText.setText("Najniższa cena: " + UsefulFunctions.getPriceFormat(bestPrice));
            outsideText.setText(String.valueOf(outside));
            footer.setVisibility(View.VISIBLE);
            setBestOffer(bestOffer);

            // Shops adapter
            ShopsAdapter shopsAdapter = new ShopsAdapter(this, R.layout.shop, shops);
            listView.setAdapter(shopsAdapter);
            shopsAdapter.notifyDataSetChanged();
        }
    }

    private void setBestOffer(final Offer bestOffer){
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.best_offer);
        TextView bestOfferTitle = (TextView) findViewById(R.id.best_offer_title);
        ImageView bestOfferPhoto = (ImageView) findViewById(R.id.best_offer_photo);
        TextView bestOfferPrice = (TextView) findViewById(R.id.best_offer_price);
        TextView bestOfferAvailability = (TextView) findViewById(R.id.best_offer_availability);

        bestOfferTitle.setText(bestOffer.getTitle());
        bestOfferPrice.setText(UsefulFunctions.getPriceFormat(bestOffer.getPrice()));
        PhotoHelper photoHelper = new PhotoHelper(bestOffer.getPhotoId(), bestOffer.getTitle(), "500x500");
        String offerUrl = photoHelper.getPhotoUrl();
        Picasso.with(this).load(offerUrl).into(bestOfferPhoto);
        switch (bestOffer.getAvailability()){
            case 0:
                bestOfferAvailability.setText("Dostępny");
                bestOfferAvailability.setTextColor(Color.GREEN);
                break;
            case 1:
                bestOfferAvailability.setText("Dostępny do tygodnia");
                bestOfferAvailability.setTextColor(Color.parseColor("#FFA3701E"));
                break;
            case 2:
                bestOfferAvailability.setText("Dostępny powyżej tygodnia");
                bestOfferAvailability.setTextColor(Color.parseColor("#FFA3701E"));
                break;
            case 3:
                bestOfferAvailability.setText("Dostępny na życzenie");
                bestOfferAvailability.setTextColor(Color.parseColor("#FFA3701E"));
                break;
            default:
                bestOfferAvailability.setText("Sprawdź w sklepie");
                bestOfferAvailability.setTextColor(Color.parseColor("#FFA3701E"));
                break;
        }

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("http://nokaut.click" + bestOffer.getClickUrl()));
                startActivity(callIntent);
            }
        });
    }
}