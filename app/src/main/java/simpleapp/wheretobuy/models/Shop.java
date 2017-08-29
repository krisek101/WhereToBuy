package simpleapp.wheretobuy.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shop implements Comparable<Shop> {

    private String id;
    private String name;
    private String url;
    private String logoUrl;
    private List<ShopLocation> locations = new ArrayList<>();
    private double bestDistance = -1;
    private double minPrice = -1;

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

    public List<ShopLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ShopLocation> locations) {
        this.locations = locations;
    }

    public double getBestDistance() {
        return bestDistance;
    }

    public void setBestDistance(double bestDistance) {
        this.bestDistance = bestDistance;
    }

    public void addLocation(ShopLocation shopLocation){
        locations.add(shopLocation);
        updateBestDistance();
    }

    public void updateBestDistance(){
        Collections.sort(locations);
        bestDistance = locations.get(0).getDistanceFromUser();
    }

    @Override
    public int compareTo(@NonNull Shop shop) {
        if (this.getBestDistance() > shop.getBestDistance()) {
            return 1;
        } else if (this.getBestDistance() < shop.getBestDistance()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null) {
            if (obj.getClass().isInstance(this)) {
                Shop obj2 = (Shop) obj;
                if (this.getName().toLowerCase().equals(obj2.getName().toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }
}