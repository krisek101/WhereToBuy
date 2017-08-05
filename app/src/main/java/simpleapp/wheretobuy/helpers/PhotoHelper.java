package simpleapp.wheretobuy.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PhotoHelper {

    private String photoId;
    private String title;
    private String size;

    public PhotoHelper(String photoId, String title, String size) {
        this.photoId = photoId;
        this.title = title;
        this.size = size;
    }

    public String getPhotoUrl(){
        String url = "http://offers.gallery/p-" + photoId.substring(0,2) + "-" + photoId.substring(2,4) + "-" + photoId + "90x90/";
        try {
            url += URLEncoder.encode(title, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url += ".jpg";
        return url;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}