package simpleapp.wheretobuy.models;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ShopLocation implements Comparable<ShopLocation> {

    private String id;
    private String name;
    private String phoneNumber;
    private LatLng location;
    private Marker marker;
    private Double distanceFromUser;
    private double rating;
    private JSONArray reviews;
    private List<String> openHours = new ArrayList<>();
    private boolean openNow;
    private List<String> photos = new ArrayList<>();
    private String website;
    private String address;

    public ShopLocation(String id, String name, LatLng location, Marker marker, Double distanceFromUser, double rating, boolean openNow) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.marker = marker;
        this.distanceFromUser = distanceFromUser;
        this.rating = rating;
        this.openNow = openNow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public JSONArray getReviews() {
        return reviews;
    }

    public void setReviews(JSONArray reviews) {
        this.reviews = reviews;
    }

    public List<String> getOpenHours() {
        return openHours;
    }

    public void setOpenHours(List<String> openHours) {
        this.openHours = openHours;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int compareTo(@NonNull ShopLocation shopLocation) {
        if (this.getDistanceFromUser() > shopLocation.getDistanceFromUser()) {
            return 1;
        } else if (this.getDistanceFromUser() < shopLocation.getDistanceFromUser()) {
            return -1;
        } else {
            return 0;
        }
    }
}