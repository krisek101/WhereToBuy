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
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.adapters.MarkerInfoWindowAdapter;
import simpleapp.wheretobuy.adapters.OffersAdapter;
import simpleapp.wheretobuy.adapters.SectionsPageAdapter;
import simpleapp.wheretobuy.adapters.ShopsAdapter;
import simpleapp.wheretobuy.constants.ClearableAutoCompleteTextView;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.fragments.TabOffersFragment;
import simpleapp.wheretobuy.fragments.TabOffersInfoWindowFragment;
import simpleapp.wheretobuy.fragments.TabShopFragment;
import simpleapp.wheretobuy.fragments.TabShopsFragment;
import simpleapp.wheretobuy.helpers.ListenerHelper;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.CustomDialog;
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
    public RequestHelper requestHelper;
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mFooterViewPager;
    private ViewPager mInfoWindowViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        prepareGoogleMap();
        requestPermissions();
        preLocation();
        initUI();
        setListeners();
        initViewPagers();
    }

    // initial functions
    private void initUI() {
        searchText = (ClearableAutoCompleteTextView) findViewById(R.id.search_text);
        searchText.setClearButton(ResourcesCompat.getDrawable(getResources(), R.drawable.clear, null), false);
        searchText.setActivity(this);
        queue = Volley.newRequestQueue(this);
        ((FloatingActionButton) findViewById(R.id.getMyLocationButton)).setImageResource(R.drawable.ic_my_location_white_24dp);
        footer = (RelativeLayout) findViewById(R.id.footer);
        footerTop = footer.getY();
        footerSlider = (RelativeLayout) findViewById(R.id.footer_content);
        footerSlider.setY(UsefulFunctions.getScreenHeight(this) - UsefulFunctions.getStatusBarHeight(this));
        footerSlider.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, UsefulFunctions.getScreenHeight(this) - footer.getLayoutParams().height - UsefulFunctions.getStatusBarHeight(this)));

    }

    private void initViewPagers(){
        //mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mFooterViewPager = (ViewPager) findViewById(R.id.footer_pager);
        setupFooterViewPager(mFooterViewPager);

        TabLayout tabFooterLayout = (TabLayout) findViewById(R.id.footer_pager_tabs);
        tabFooterLayout.setupWithViewPager(mFooterViewPager);
    }

    private void setupFooterViewPager(ViewPager viewPager){
        SectionsPageAdapter sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        sectionsPageAdapter.addFragment(new TabOffersFragment(), "Oferty");
        sectionsPageAdapter.addFragment(new TabShopsFragment(), "Sklepy");
        viewPager.setAdapter(sectionsPageAdapter);
    }

    private void setupInfoWindowViewPager(ViewPager viewPager){
        SectionsPageAdapter sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        sectionsPageAdapter.addFragment(new TabOffersInfoWindowFragment(), "Oferty");
        sectionsPageAdapter.addFragment(new TabShopFragment(), "O sklepie");
        viewPager.setAdapter(sectionsPageAdapter);
    }

    public void setLoading(boolean load) {
        if (load) {
            try {
                GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.loading);
                searchText.setLoadingGif(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
                clearResults();

                // update list from Nokaut API
                requestHelper = new RequestHelper(Constants.TAG_CATEGORY, MapActivity.this);
                requestHelper.setCategoryAutocompleteUrl(s.toString());
                requestHelper.doRequest("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void clearResults() {
        clearShopMarkers();
        shops.clear();
        offers.clear();
        autoCompleteResults.clear();
        changeFooterInfo();

        // cancel queue
        if (queue != null) {
            queue.cancelAll(Constants.TAG_AUTOCOMPLETE);
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
        if (userLocation != null) {
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

                    // change distance shops from user
                    List<Float> distancesFromUser = new ArrayList<>();
                    if (!shops.isEmpty()) {
                        for (Shop s : shops) {
                            distancesFromUser.clear();
                            if (s.getLocations() != null) {
                                for (LatLng loc : s.getLocations()) {
                                    distancesFromUser.add(UsefulFunctions.getDistanceBetween(loc, userLocation));
                                }
                            }
                            s.setDistancesFromUser(distancesFromUser);
                        }
                    }

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

    @Override
    public void onBackPressed() {
        if (footerOpened) {
            float toY = UsefulFunctions.getScreenHeight(this) - UsefulFunctions.getStatusBarHeight(this);
            footerOpened = false;
            footerSlider.animate().y(toY).setDuration(100).start();
            footer.animate().y(toY - footer.getHeight()).setDuration(100).start();
        } else {
            super.onBackPressed();
        }
    }

    private void restoreData() {
        // last location
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("Latitude", "").isEmpty() && !PreferenceManager.getDefaultSharedPreferences(this).getString("Longitude", "").isEmpty()) {
            userLocation = new LatLng(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Latitude", "No Latitude Value Stored")), Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("Longitude", "No Longitude Value Stored")));
            setUserLocationMarker();
        }
    }

    public void hideKeyboard() {
        View view = MapActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Offers
    public List<Offer> getOffersByLocation(LatLng position) {
        List<Offer> offersByPosition = new ArrayList<>();
        for (Offer o : offers) {
            if (o.getShop().getLocations() != null) {
                if (o.getShop().getLocations().contains(position)) {
                    offersByPosition.add(o);
                }
            }
        }
        return offersByPosition;
    }

    public void showOffersInAlertDialog(List<Offer> offersByPosition, LatLng shopLocation) {
        // alert dialog and inflater
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View convertView = inflater.inflate(R.layout.offers_list_window, null);

        CustomDialog newFragment = CustomDialog.newInstance(offersByPosition, shopLocation, this);
        newFragment.show(getSupportFragmentManager(), "dialog");

//         UI

    }

    public void changeFooterInfo() {
        LinearLayout floatingButtonContainer = (LinearLayout) findViewById(R.id.floating_button_container);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) floatingButtonContainer.getLayoutParams();
        if (offers.size() == 0) {
            footer.setVisibility(View.GONE);
            lp.setMargins(0, 0, UsefulFunctions.getPixelsFromDp(this, 25), UsefulFunctions.getPixelsFromDp(this, 25));
        } else {
            // logic
            Collections.sort(offers);
            Offer bestOffer = offers.get(0);
            int nearMe, outside = 0;
            for (Offer offer : offers) {
                if (offer.getShop().getLocations() == null || (offer.getShop().getLocations() != null && offer.getShop().getLocations().isEmpty())) {
                    outside++;
                }
            }
            nearMe = offers.size() - outside;

            // UI
            lp.setMargins(0, 0, UsefulFunctions.getPixelsFromDp(this, 25), UsefulFunctions.getPixelsFromDp(this, 70));
            TextView nearMeText = (TextView) findViewById(R.id.near_me_text);
            TextView bestPriceText = (TextView) findViewById(R.id.best_price_text);
            TextView outsideText = (TextView) findViewById(R.id.outside_text);
            bestPriceText.setTypeface(null, Typeface.BOLD);
            ListView shopsListView = (ListView) findViewById(R.id.footer_shops);
            ListView offersListView = (ListView) findViewById(R.id.footer_offers);


            // Setters
            nearMeText.setText(String.valueOf(nearMe));
            bestPriceText.setText("Najlepsza oferta: " + UsefulFunctions.getPriceFormat(bestOffer.getPrice()));
            outsideText.setText(String.valueOf(outside));
//            setBestOffer(bestOffer);

            // Shops
            List<Shop> shopsOnMap = new ArrayList<>();
            List<Shop> shopsOutside = new ArrayList<>();

            // Shops - order
            for (Shop s : shops) {
                if (s.getMarkers() != null) {
                    if (!s.getMarkers().isEmpty()) {
                        shopsOnMap.add(s);
                    } else {
                        shopsOutside.add(s);
                    }
                } else {
                    shopsOutside.add(s);
                }
            }
            Collections.sort(shopsOnMap);
            shops.clear();
            shops.addAll(shopsOnMap);
            shops.addAll(shopsOutside);

            // Shops - adapter
            ShopsAdapter shopsAdapter = new ShopsAdapter(this, R.layout.shop, shops, this);
            shopsListView.setAdapter(shopsAdapter);
            shopsAdapter.notifyDataSetChanged();

            // Offers - adapter
            OffersAdapter offersAdapter = new OffersAdapter(this, R.layout.offer, offers, "offer_footer");
            offersListView.setAdapter(offersAdapter);
            offersAdapter.notifyDataSetChanged();

            // Footer Visibility
            footer.setVisibility(View.VISIBLE);
        }
        floatingButtonContainer.setLayoutParams(lp);
    }

    // Shops
    public void clearShopMarkers() {
        for (Shop shop : shops) {
            if (shop.getMarkers() != null) {
                for (Marker m : shop.getMarkers()) {
                    m.remove();
                }
            }
        }
    }
}