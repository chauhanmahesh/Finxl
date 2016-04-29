package com.finxl.finxlsample.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Mahesh Chauhan on 4/27/2016.
 * Model class to encapsulate the single Fact detail which is coming from the json Data.
 */
public class Fact {
    @Expose
    @SerializedName("title")
    private String title = null;
    @Expose
    @SerializedName("description")
    private String description = null;
    @Expose
    @SerializedName("imageHref")
    private String imageUrl = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
