package simpleapp.wheretobuy.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Shop {

    private String name;
    private String url;
    private String logoUrl;
    private String id;
    private List<LatLng> locations;

    public Shop(String name, String url, String logoUrl, String id) {
        this.name = name;
        this.url = url;
        this.logoUrl = logoUrl;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(List<LatLng> locations) {
        this.locations = locations;
    }
}