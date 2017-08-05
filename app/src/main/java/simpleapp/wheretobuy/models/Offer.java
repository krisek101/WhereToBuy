package simpleapp.wheretobuy.models;

public class Offer {

    private int availability;
    private int price;
    private String title;
    private String category;
    private String desc;
    private String clickUrl;
    private String producer;
    private String photoId;
    private Shop shop;

    public Offer(int availability, int price, String title, String category, String desc, String clickUrl, String producer, String photoId) {
        this.availability = availability;
        this.price = price;
        this.title = title;
        this.category = category;
        this.desc = desc;
        this.clickUrl = clickUrl;
        this.producer = producer;
        this.photoId = photoId;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
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
}