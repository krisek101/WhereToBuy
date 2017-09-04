package simpleapp.wheretobuy.models;

import android.support.annotation.NonNull;

public class Offer implements Comparable<Offer> {

    private int availability;
    private double price;
    private String title;
    private String category;
    private String desc;
    private String clickUrl;
    private String producer;
    private String photoId;
    private Shop shop;
    private String type;
    private String id;

    public Offer(String type, int availability, double price, String title, String category, String desc, String clickUrl, String producer, String photoId) {
        this.type = type;
        this.availability = availability;
        this.price = price;
        this.title = title;
        this.category = category;
        this.desc = desc;
        this.clickUrl = clickUrl;
        this.producer = producer;
        this.photoId = photoId;
    }

    public Offer(String type, int availability, String title, String clickUrl, String category, String photoId, String id) {
        this.type = type;
        this.availability = availability;
        this.title = title;
        this.category = category;
        this.clickUrl = clickUrl;
        this.photoId = photoId;
        this.id = id;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull Offer offer) {
        if (this.getPrice() > offer.getPrice()) {
            return 1;
        } else if (this.getPrice() < offer.getPrice()) {
            return -1;
        } else {
            if (this.getShop().getBestDistance() != -1 && offer.getShop().getBestDistance() != -1) {
                if (this.getShop().getBestDistance() > offer.getShop().getBestDistance()) {
                    return 1;
                } else if (this.getShop().getBestDistance() < offer.getShop().getBestDistance()) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null) {
            if (obj.getClass().isInstance(this)) {
                Offer obj2 = (Offer) obj;
                if (this.getTitle().equals(obj2.getTitle())) {
                    if(this.getShop() != null && obj2.getShop() != null) {
                        if (this.getShop().equals(obj2.getShop())) {
                            if (this.getPrice() == obj2.getPrice()) {
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
}