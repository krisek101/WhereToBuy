package simpleapp.wheretobuy.constants;

public class Constants {

    // Permissions request codes
    public static final int REQUEST_PERMISSIONS_CODE = 2;
    public static final int REQUEST_CHECK_PLAY_SERVICES = 3;
    public static final int REQUEST_CHECK_SETTINGS = 4;
    public static final int SPEECH_REQUEST_CODE = 1;

    // Tags
    public static final String TAG_AUTOCOMPLETE = "TAG_AUTOCOMPLETE";
    public static final String TAG_CATEGORY = "TAG_CATEGORY";
    public static final String TAG_RESULT_DETAILS = "TAG_RESULT_DETAILS";
    public static final String TAG_PLACES = "TAG_PLACES";
    public static final String TAG_TESCO_AUTOCOMPLETE = "TAG_TESCO_AUTOCOMPLETE";

    // Tokens and Keys
    public static final String NOKAUT_TOKEN = "1/c1d3e6f5f27737fe2cb0dfd5b0046743ae4297da1c446fbe1219b8db7475367c";
    public static final String WEB_API_GOOGLE_KEY = "AIzaSyDflSpLotdTPuGtyeZEPNHHNZGVDsft040";
    public static final String WEB_API_TESCO_PRIMARY_KEY = "fb5f816afe6d4b7c88f7f9d61da04f05";
    public static final String WEB_API_TESCO_SECONDARY_KEY = "4322134ceb37456988139b06eeffefaa";

    // Constants
    public static final int SPLASH_TIME = 1000;
    public static final String UNKNOWN_ADDRESS = "Adres nieznany";
    public static final String[] UNACCEPTABLE_SHOPS = {"mall", "sklep bielsko-biała", "twójlombard"};
    public static final String[] PARTNERS = {"google", "nokaut", "tesco"};
    public static final int GOOGLE_SEARCH_RADIUS = 10000;
    public static final String NEARBY_SEARCH = "NEARBY_SEARCH";
}