package simpleapp.wheretobuy.models;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class Shop implements Comparable<Shop> {

    private String name;
    private String url;
    private String logoUrl;
    private String id;
    private List<LatLng> locations;
    private List<Marker> markers;
    private List<Float> distancesFromUser = new ArrayList<>();
    private Float bestDistance;

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

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public List<Float> getDistancesFromUser() {
        return distancesFromUser;
    }

    public void setDistancesFromUser(List<Float> distancesFromUser) {
        this.distancesFromUser = distancesFromUser;
        this.bestDistance = 1000000f;
        for(Float f : distancesFromUser){
            if(f<bestDistance) f = bestDistance;
        }
    }

    public Float getBestDistance() {
        return bestDistance;
    }

    public void setBestDistance(Float bestDistance) {
        this.bestDistance = bestDistance;
    }

    @Override
    public int compareTo(@NonNull Shop shop) {
        if (this.bestDistance != null && shop.bestDistance != null) {
            if (this.bestDistance > shop.bestDistance) {
                return 1;
            } else if (this.bestDistance < shop.bestDistance) {
                return -1;
            } else {
                return 0;
            }
        } else{
            return 0;
        }
    }
}