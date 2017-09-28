package simpleapp.wheretobuy.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.adapters.MarkerInfoWindowAdapter;
import simpleapp.wheretobuy.adapters.OffersAdapter;
import simpleapp.wheretobuy.adapters.SectionsPageAdapter;
import simpleapp.wheretobuy.adapters.ShopsAdapter;
import simpleapp.wheretobuy.constants.ClearableAutoCompleteTextView;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;
import simpleapp.wheretobuy.fragments.TabOffersFragment;
import simpleapp.wheretobuy.fragments.TabShopsFragment;
import simpleapp.wheretobuy.helpers.ListenerHelper;
import simpleapp.wheretobuy.helpers.LoadingHelper;
import simpleapp.wheretobuy.models.AutoCompleteResult;
import simpleapp.wheretobuy.models.CustomDialog;
import simpleapp.wheretobuy.models.Offer;
import simpleapp.wheretobuy.models.Shop;
import simpleapp.wheretobuy.models.ShopLocation;
import simpleapp.wheretobuy.partners.GooglePartner;
import simpleapp.wheretobuy.partners.NokautPartner;
import simpleapp.wheretobuy.partners.SkapiecPartner;
import simpleapp.wheretobuy.tasks.GeocodeTask;

import static simpleapp.wheretobuy.constants.Constants.SPEECH_REQUEST_CODE;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Location and map
    public GoogleMap mMap;
    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    public LatLng userLocation;
    private Marker userLocationMarker;

    // UI
    public FloatingSearchView searchText;
    public ClearableAutoCompleteTextView searchLocation;
    public RelativeLayout footer;
    public boolean footerOpened = false;
    public RelativeLayout footerSlider;
    public float footerTop;
    public FloatingActionButton goToShopButton;
    public FloatingActionButton getMyLocationButton;
    public TextView getMyLocation;
    public TextView changeLocation;
    public TextView stateStart;
    public RelativeLayout stateLoading;
    public RelativeLayout stateFinish;

    // Collections
    public List<AutoCompleteResult> autoCompleteResults = new ArrayList<>();
    public List<Offer> offers = new ArrayList<>();
    public List<Shop> shops = new ArrayList<>();
    public List<AutoCompleteResult> lastAutoCompleteResults = new ArrayList<>();

    // Others
    public RequestQueue queue;
    public LatLng tempPosition;
    public boolean getMyLocationButtonClicked = false;
    public AlertDialog changeLocationDialog;
    public ShopsAdapter shopsAdapter;
    public ListView shopsListView;
    public LoadingHelper loadingHelper;
    public boolean isOnline;

    // Partners
    public GooglePartner googleHelper;
    public NokautPartner nokautHelper;
    public SkapiecPartner skapiecHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        isOnline = UsefulFunctions.isOnline(MapActivity.this);
        prepareGoogleMap();
        requestPermissions();
        initUI();
        preLocation();
        initPartners();
        setListeners();
        initViewPagers();
        changeFooterInfo();
//        checkOnline();
    }

    private void initPartners() {
        googleHelper = new GooglePartner("https://maps.googleapis.com/maps/api/place/", this);
        nokautHelper = new NokautPartner("http://nokaut.io/api/v2/", this);
        skapiecHelper = new SkapiecPartner("http://api.skapiec.pl/beta_", this);
    }

    private void checkOnline() {
        final Handler h = new Handler();
        final int delay = 1000;
        final Runnable[] runnable = new Runnable[1];
        final boolean[] already = {true, true};
        h.postDelayed(new Runnable() {
            public void run() {
                if (UsefulFunctions.isOnline(MapActivity.this)) {
                    if (already[0]) {
                        already[0] = false;
                        already[1] = true;
                        isOnline = true;
                        changeFooterInfo();
                        if (userLocation == null) {
                            preLocation();
                        }
                    }
                } else {
                    if (already[1]) {
                        already[1] = false;
                        already[0] = true;
                        isOnline = false;
                        changeFooterInfo();
                    }
                }
                runnable[0] = this;
                h.postDelayed(runnable[0], delay);
            }
        }, delay);
    }

    // initial functions
    private void initUI() {
        searchText = (FloatingSearchView) findViewById(R.id.search_text);
        queue = Volley.newRequestQueue(this);
        footer = (RelativeLayout) findViewById(R.id.footer);
        footerTop = footer.getY();
        footerSlider = (RelativeLayout) findViewById(R.id.footer_content);
        footerSlider.setY(UsefulFunctions.getScreenHeight(this) - UsefulFunctions.getStatusBarHeight(this));
        footerSlider.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, UsefulFunctions.getScreenHeight(this) - footer.getLayoutParams().height - UsefulFunctions.getStatusBarHeight(this)));
        goToShopButton = (FloatingActionButton) findViewById(R.id.goToShopButton);
        getMyLocationButton = (FloatingActionButton) findViewById(R.id.getMyLocationButton);
        getMyLocation = (TextView) findViewById(R.id.getMyLocation);
        changeLocation = (TextView) findViewById(R.id.goToSearchingCentre);
        stateStart = (TextView) findViewById(R.id.state_start);
        stateLoading = (RelativeLayout) findViewById(R.id.state_loading);
        stateFinish = (RelativeLayout) findViewById(R.id.state_finish);
        initSearchText();
    }

    private void initViewPagers() {
        ViewPager mFooterViewPager = (ViewPager) findViewById(R.id.footer_pager);
        setupFooterViewPager(mFooterViewPager);

        TabLayout tabFooterLayout = (TabLayout) findViewById(R.id.footer_pager_tabs);
        tabFooterLayout.setupWithViewPager(mFooterViewPager);
    }

    private void setupFooterViewPager(ViewPager viewPager) {
        SectionsPageAdapter sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        sectionsPageAdapter.addFragment(new TabOffersFragment(), "Oferty");
        sectionsPageAdapter.addFragment(new TabShopsFragment(), "Sklepy");
        viewPager.setAdapter(sectionsPageAdapter);
    }

    public void showFabOptions() {
        getMyLocationButtonClicked = true;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getMyLocation.getLayoutParams();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) changeLocation.getLayoutParams();

        layoutParams.setMargins(0, 0, UsefulFunctions.getPixelsFromDp(this, 70), UsefulFunctions.getPixelsFromDp(this, 125));
        layoutParams2.setMargins(0, 0, UsefulFunctions.getPixelsFromDp(this, 90), UsefulFunctions.getPixelsFromDp(this, 85));

        getMyLocation.setLayoutParams(layoutParams);
        changeLocation.setLayoutParams(layoutParams2);


        Animation show_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.location_fab_show);

        getMyLocation.setVisibility(View.VISIBLE);
        getMyLocation.startAnimation(show_fab);
        getMyLocation.setClickable(true);

        changeLocation.setVisibility(View.VISIBLE);
        changeLocation.startAnimation(show_fab);
        changeLocation.setClickable(true);
    }

    public void hideFabOptions() {
        getMyLocationButtonClicked = false;
        Animation hide_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.location_fab_hide);

        getMyLocation.setVisibility(View.GONE);
        getMyLocation.startAnimation(hide_fab);
        getMyLocation.setClickable(false);

        changeLocation.setVisibility(View.GONE);
        changeLocation.startAnimation(hide_fab);
        changeLocation.setClickable(false);
    }

    public void changeUserLocation() {
        final AlertDialog.Builder placeInfoWindow = new AlertDialog.Builder(MapActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.search_location_window, null);
        placeInfoWindow.setView(convertView);
        changeLocationDialog = placeInfoWindow.show();
        if (changeLocationDialog.getWindow() != null) {
            changeLocationDialog.getWindow().getAttributes().verticalMargin = -0.2F;
        }
        if (changeLocationDialog.getWindow() != null) {
            changeLocationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        searchLocation = (ClearableAutoCompleteTextView) convertView.findViewById(R.id.search_location);
        searchLocation.setClearButton(ResourcesCompat.getDrawable(getResources(), R.drawable.clear, null), false);
        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (queue != null) {
                    queue.cancelAll(Constants.TAG_SEARCH_ADDRESS_AUTOCOMPLETE);
                }
                googleHelper.showAutoCompleteAddresses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setListeners() {
        ListenerHelper listenerHelper = new ListenerHelper(this);
        listenerHelper.setListener(getMyLocationButton, "click");
        listenerHelper.setListener(goToShopButton, "click");
        listenerHelper.setListener(footer, "touch");
        listenerHelper.setListener(getMyLocation, "click");
        listenerHelper.setListener(changeLocation, "click");
    }

    private void initSearchText() {
        loadingHelper = new LoadingHelper(this);
        searchText.setDismissFocusOnItemSelection(true);

        // on clear button click
        searchText.setOnClearSearchActionListener(new FloatingSearchView.OnClearSearchActionListener() {
            @Override
            public void onClearSearchClicked() {
                clearResults();
                searchText.hideProgress();
                hideFabOptions();
            }
        });

        // on microphone click
        searchText.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_voice:
                        String language = "pl-PL";
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                        startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE);
                        break;
                }
            }
        });

        // show auto complete results on change
        searchText.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                clearResults();
                if (newQuery.length() <= 2) {
                    if (lastAutoCompleteResults != null) {
                        for (AutoCompleteResult autoCompleteResult : lastAutoCompleteResults) {
                            if (autoCompleteResult.getName().toLowerCase().startsWith(newQuery.toLowerCase())) {
                                autoCompleteResults.add(autoCompleteResult);
                            }
                        }
                        searchText.swapSuggestions(autoCompleteResults);
                    }
                } else {
                    if (UsefulFunctions.isOnline(MapActivity.this)) {
                        // update list from Nokaut API
                        searchText.showProgress();
                        nokautHelper.showAutoCompleteProducts(newQuery);
                    } else {
                        List<AutoCompleteResult> noInternet = new ArrayList<>();
                        noInternet.add(new AutoCompleteResult("error", "Brak połączenia z Internetem", "noInternetConn"));
                        searchText.swapSuggestions(noInternet);
                    }
                }
            }
        });

        // on suggestion click
        searchText.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                AutoCompleteResult autoCompleteResult = (AutoCompleteResult) searchSuggestion;
                if (!autoCompleteResult.getType().equals("error")) {
                    if (autoCompleteResult.getId().equals("all")) {
                        autoCompleteResults.clear();
                        nokautHelper.getMoreProducts(autoCompleteResult.getName(), 0);
                    } else {
                        nokautHelper.getOffers(autoCompleteResult);
                    }
                    skapiecHelper.searchMostCommonCategory(autoCompleteResult.getName(), 0);
                    addLastAutoCompleteResult(autoCompleteResult);
//                    changeFooterInfo();
                } else {
                    autoCompleteResult.setName("");
                }
            }

            @Override
            public void onSearchAction(String currentQuery) {
            }
        });

        searchText.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                AutoCompleteResult autoCompleteResult = (AutoCompleteResult) item;
                if (lastAutoCompleteResults != null) {
                    if (lastAutoCompleteResults.contains(autoCompleteResult)) {
                        leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_history_black_24dp, null));
                        Util.setIconColor(leftIcon, Color.parseColor("#000000"));
                        leftIcon.setAlpha(.36f);
                    } else {
                        leftIcon.setAlpha(0.0f);
                        leftIcon.setImageDrawable(null);
                    }
                }
                textView.setTextColor(Color.parseColor("#000000"));
                String text = autoCompleteResult.getBody().toLowerCase().replaceFirst(searchText.getQuery().toLowerCase(), "<font color=\"" + "#787878" + "\">" + searchText.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }
        });

        searchText.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                clearResults();
                if (lastAutoCompleteResults != null) {
                    searchText.swapSuggestions(lastAutoCompleteResults);
                }
            }

            @Override
            public void onFocusCleared() {

            }
        });
    }

    private void setAutocompleteListByInput(AutoCompleteResult autoCompleteResult) {

    }

    private void addLastAutoCompleteResult(AutoCompleteResult autoCompleteResult) {
        if (lastAutoCompleteResults != null) {
            if (lastAutoCompleteResults.size() == Constants.MAX_AUTOCOMPLETE_RESULTS) {
                lastAutoCompleteResults.remove(lastAutoCompleteResults.size() - 1);
            }
            if (!lastAutoCompleteResults.contains(autoCompleteResult)) {
                lastAutoCompleteResults.add(autoCompleteResult);
            }
            Gson gson = new Gson();
            String json = gson.toJson(lastAutoCompleteResults);
            PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("LastAutocompleteResults", json).apply();
        }
    }

    public void hardClear() {
        searchText.setSearchText("");
        if (getMyLocationButtonClicked) {
            hideFabOptions();
        }
        if (queue != null) {
            queue.cancelAll(Constants.TAG_AUTOCOMPLETE_BY_CATEGORY);
        }
        clearResults();
    }

    public void clearResults() {
        // cancel queue
        if (queue != null) {
            queue.cancelAll(Constants.TAG_SHOW_AUTOCOMPLETE_PRODUCTS);
            loadingHelper.clearAllRequests();
        }
        searchText.hideProgress();
        goToShopButton.setVisibility(View.GONE);
        clearShopMarkers();
        shops.clear();
        autoCompleteResults.clear();
        offers.clear();
        changeFooterInfo();
        searchText.clearSuggestions();
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
            showCommunicationSearchingLocation();
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
        } else {
            stateStart.setTextColor(getResources().getColor(R.color.red));
            stateStart.setText(R.string.error_internet_conn);
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
                    // save old location
                    LatLng oldLocation = new LatLng(-1, -1);
                    if (userLocation != null) {
                        oldLocation = userLocation;
                    }

                    // set lat and lng
                    double latitude = mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    userLocation = new LatLng(latitude, longitude);
                    new GeocodeTask(this, userLocation, stateStart).execute();

                    // if change location is more than 100 meters, then refresh searching
                    if (oldLocation.latitude != -1) {
                        double distance = UsefulFunctions.getDistanceBetween(oldLocation, userLocation);
                        if (distance > 100) {
                            refreshShopLocations();
                        }
                    }

                    // change distance shops from user
                    if (!shops.isEmpty()) {
                        for (Shop s : shops) {
                            if (!s.getLocations().isEmpty()) {
                                for (ShopLocation shopLocation : s.getLocations()) {
                                    shopLocation.setDistanceFromUser(UsefulFunctions.getDistanceBetween(shopLocation.getLocation(), userLocation));
                                }
                                s.updateBestDistance();
                            }
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
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!marker.equals(userLocationMarker)) {
                    ShopLocation shopLocation = getShopLocationByPosition(marker.getPosition());
                    Shop shop = getShopByShopLocation(shopLocation);
                    List<Offer> offersByShop = getOffersByShop(shop);
                    showCustomDialog(offersByShop, shopLocation);
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.equals(userLocationMarker)) {
                    tempPosition = marker.getPosition();
                    goToShopButton.setVisibility(View.VISIBLE);
                } else {
                    goToShopButton.setVisibility(View.GONE);
                }
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (getMyLocationButtonClicked) {
                    hideFabOptions();
                }
                goToShopButton.setVisibility(View.GONE);
                hideKeyboard();
            }
        });

        restoreData();
    }

    // Others
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            searchText.setSearchFocused(true);
            searchText.setSearchText(spokenText);
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
        } else if (getMyLocationButtonClicked) {
            hideFabOptions();
        } else if (!offers.isEmpty()) {
            hardClear();
        } else if (searchText.isFocused()) {
            searchText.clearSuggestions();
            searchText.clearFocus();
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
        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("LastAutocompleteResults", "").isEmpty()) {
            Gson gson = new Gson();
            String jsonPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("LastAutocompleteResults", "");
            Type type = new TypeToken<List<AutoCompleteResult>>() {
            }.getType();
            lastAutoCompleteResults = gson.fromJson(jsonPreferences, type);
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
    public void showCustomDialog(List<Offer> offersList, ShopLocation shopLocation) {
        if(!offersList.isEmpty()) {
            CustomDialog newFragment = CustomDialog.newInstance(offersList, shopLocation, this);
            newFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    public void showCommunicationSearchingLocation() {
        stateStart.setTextColor(getResources().getColor(R.color.black));
        stateStart.setText(R.string.state_localization);
    }

    public void changeFooterInfo() {
        if (autoCompleteResults.isEmpty()) {
            // set Visibility
            stateStart.setVisibility(View.VISIBLE);
            stateLoading.setVisibility(View.GONE);
            stateFinish.setVisibility(View.GONE);
            // show address
            showCommunicationSearchingLocation();
            if (isOnline) {
                if (userLocation != null) {
                    new GeocodeTask(this, userLocation, stateStart).execute();
                }
            } else {
                stateStart.setTextColor(getResources().getColor(R.color.red));
                stateStart.setText(R.string.error_internet_conn);
            }
        } else {
            if (loadingHelper.isLoading) {
                // set Visibility
                stateFinish.setVisibility(View.GONE);
                stateStart.setVisibility(View.GONE);
                stateLoading.setVisibility(View.VISIBLE);
            } else if (!offers.isEmpty()) {
                // set Visibility
                stateStart.setVisibility(View.GONE);
                stateLoading.setVisibility(View.GONE);
                stateFinish.setVisibility(View.VISIBLE);

                TextView leftText = (TextView) findViewById(R.id.near_me_text);
                TextView middleText = (TextView) findViewById(R.id.best_price_text);
                TextView rightText = (TextView) findViewById(R.id.outside_text);
                middleText.setTypeface(null, Typeface.BOLD);

                final OffersAdapter offersAdapter = new OffersAdapter(this, R.layout.offer, offers, "offer_footer");
                shopsAdapter = new ShopsAdapter(this, R.layout.shop, shops, this);
                int outside, nearMe = 0;
                for (Shop s : shops) {
                    if(s.getBestDistance() != -1){
                        nearMe += getOffersByShop(s).size();
                    }
                }
                outside = offers.size() - nearMe;

                // UI - footer shops
                shopsListView = (ListView) findViewById(R.id.footer_shops);
                final Button sortShopsByPriceView = (Button) findViewById(R.id.sort_shops_by_price);
                final Button sortShopsByDistanceView = (Button) findViewById(R.id.sort_shops_by_distance);
                final String[] sortShopsStatus = {Constants.SORT_BY_DISTANCE};

                // UI - footer offers
                final ListView offersListView = (ListView) findViewById(R.id.footer_offers);
                final Button sortOffersByPriceView = (Button) findViewById(R.id.sort_by_price);
                final Button sortOffersByDistanceView = (Button) findViewById(R.id.sort_by_distance);
                final String[] sortOffersStatus = {Constants.SORT_BY_PRICE};
                final RelativeLayout offersFilters = (RelativeLayout) findViewById(R.id.filters_offers);
                offersFilters.setVisibility(View.VISIBLE);
                changeFilterClicked(1, sortOffersByDistanceView, sortOffersByPriceView);
                changeFilterClicked(0, sortShopsByDistanceView, sortShopsByPriceView);


                // Offers - listeners
                sortOffersByPriceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (sortOffersStatus[0].equals(Constants.SORT_BY_DISTANCE)) {
                            changeFilterClicked(1, sortOffersByDistanceView, sortOffersByPriceView);
                            sortOffersStatus[0] = Constants.SORT_BY_PRICE;
                            sortOffers(sortOffersStatus[0], offersAdapter, offersListView);
                        }
                    }
                });
                sortOffersByDistanceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (sortOffersStatus[0].equals(Constants.SORT_BY_PRICE)) {
                            changeFilterClicked(0, sortOffersByDistanceView, sortOffersByPriceView);
                            sortOffersStatus[0] = Constants.SORT_BY_DISTANCE;
                            sortOffers(sortOffersStatus[0], offersAdapter, offersListView);
                        }
                    }
                });

                // Shops - listeners
                sortShopsByPriceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (sortShopsStatus[0].equals(Constants.SORT_BY_DISTANCE)) {
                            changeFilterClicked(1, sortShopsByDistanceView, sortShopsByPriceView);
                            sortShopsStatus[0] = Constants.SORT_BY_PRICE;
                            sortShops(sortShopsStatus[0]);
                        }
                    }
                });
                sortShopsByDistanceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (sortShopsStatus[0].equals(Constants.SORT_BY_PRICE)) {
                            changeFilterClicked(0, sortShopsByDistanceView, sortShopsByPriceView);
                            sortShopsStatus[0] = Constants.SORT_BY_DISTANCE;
                            sortShops(sortShopsStatus[0]);
                        }
                    }
                });

                // Setters - footer handler
                leftText.setText(String.valueOf(nearMe));
                rightText.setText(String.valueOf(outside));

                // Shops
                sortShops(sortShopsStatus[0]);

                // Offers
                sortOffers(sortOffersStatus[0], offersAdapter, offersListView);
                middleText.setText(getString(R.string.best_offer) + " " + UsefulFunctions.getPriceFormat(offers.get(0).getPrice()) + "");
            }
        }
    }

    private void sortOffers(String sortType, OffersAdapter offersAdapter, ListView offersListView) {
        switch (sortType) {
            case Constants.SORT_BY_PRICE:
                Collections.sort(offers);
                break;
            case Constants.SORT_BY_DISTANCE:
                List<Offer> offersOnMap = new ArrayList<>();
                List<Offer> offersOutside = new ArrayList<>();
                for (Offer o : offers) {
                    if (o.getShop().getBestDistance() == -1) {
                        offersOutside.add(o);
                    } else {
                        offersOnMap.add(o);
                    }
                }
                boolean war = true;
                while (war) {
                    war = false;
                    for (int i = 0; i < offersOnMap.size() - 1; i++) {
                        Offer tempOffer;
                        if (offersOnMap.get(i).getShop().getBestDistance() > offersOnMap.get(i + 1).getShop().getBestDistance()) {
                            tempOffer = offersOnMap.get(i);
                            offersOnMap.set(i, offersOnMap.get(i + 1));
                            offersOnMap.set(i + 1, tempOffer);
                            war = true;
                        }
                    }
                }
                offers.clear();
                offers.addAll(offersOnMap);
                offers.addAll(offersOutside);
                break;
        }
        offersListView.setAdapter(offersAdapter);
        offersAdapter.notifyDataSetChanged();
    }

    private void sortShops(String sortType) {
        switch (sortType) {
            case Constants.SORT_BY_PRICE:
                quickSortShops(0, shops.size() - 1);
                break;
            case Constants.SORT_BY_DISTANCE:
                List<Shop> shopsOnMap = new ArrayList<>();
                List<Shop> shopsOutside = new ArrayList<>();
                for (Shop s : shops) {
                    if (s.getBestDistance() != -1) {
                        shopsOnMap.add(s);
                    } else {
                        shopsOutside.add(s);
                    }
                }
                Collections.sort(shopsOnMap);
                shops.clear();
                shops.addAll(shopsOnMap);
                shops.addAll(shopsOutside);
                break;
        }
        shopsAdapter.notifyDataSetChanged();
        shopsListView.setAdapter(shopsAdapter);
        shopsListView.requestLayout();
    }

    private void quickSortShops(int low, int high) {
        int i = low, j = high;
        Shop pivot = shops.get(low + (high - low) / 2);
        while (i <= j) {
            while (shops.get(i).getMinPrice() < pivot.getMinPrice()) {
                i++;
            }
            while (shops.get(j).getMinPrice() > pivot.getMinPrice()) {
                j--;
            }
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quickSortShops(low, j);
        if (i < high)
            quickSortShops(i, high);
    }

    private void exchange(int i, int j) {
        Shop temp = shops.get(i);
        shops.set(i, shops.get(j));
        shops.set(j, temp);
    }

    private void changeFilterClicked(int pos, TextView textView1, TextView textView2) {
        switch (pos) {
            case 0:
                textView1.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.rounded_clicked));
                textView1.setTextColor(Color.BLACK);
                textView2.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.info_window_rounded));
                textView2.setTextColor(Color.parseColor("#767676"));
                break;
            case 1:
                textView1.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.info_window_rounded));
                textView1.setTextColor(Color.parseColor("#767676"));
                textView2.setBackground(ContextCompat.getDrawable(MapActivity.this, R.drawable.rounded_clicked));
                textView2.setTextColor(Color.BLACK);
                break;
        }
    }

    public void refreshShopLocations() {
        if (!shops.isEmpty()) {
            List<Shop> goodShops = new ArrayList<>(shops);
            clearShopMarkers();
            for (Shop shop : shops) {
                for (int p = 0; p < Constants.BLACK_LIST_SHOPS.length; p++) {
                    if (shop.getName().toLowerCase().equals(Constants.BLACK_LIST_SHOPS[p].toLowerCase())) {
                        goodShops.remove(shop);
                    }
                }
                shop.getLocations().clear();
                shop.setBestDistance(-1);
            }

            if (!goodShops.isEmpty()) {
                for (int i = 0; i < goodShops.size() - 1; i++) {
                    Shop shop = goodShops.get(i);
                    googleHelper.searchNearbyShops(shop, Constants.SEARCH_RADIUS);
                }
            }
        }
    }

    // Shops
    public void clearShopMarkers() {
        for (Shop shop : shops) {
            if (!shop.getLocations().isEmpty()) {
                for (ShopLocation shopLocation : shop.getLocations()) {
                    shopLocation.getMarker().remove();
                    shopLocation.setMarker(null);
                }
            }
        }
    }

    public ShopLocation getShopLocationByPosition(LatLng position) {
        for (Shop s : shops) {
            for (ShopLocation shopLocation : s.getLocations()) {
                if (shopLocation.getLocation().equals(position)) {
                    return shopLocation;
                }
            }
        }
        return null;
    }

    public Shop getShopByShopLocation(ShopLocation shopLocation) {
        for (Shop s : shops) {
            for (ShopLocation sl : s.getLocations()) {
                if (sl.equals(shopLocation)) {
                    return s;
                }
            }
        }
        return null;
    }

    public List<Offer> getOffersByShop(Shop shop) {
        List<Offer> offersByShop = new ArrayList<>();
        List<Offer> tempOffers = new ArrayList<>(offers);
        for(Offer o : tempOffers){
            if (o.getShop().equals(shop)) {
                offersByShop.add(o);
            }
        }
        return offersByShop;
    }
}