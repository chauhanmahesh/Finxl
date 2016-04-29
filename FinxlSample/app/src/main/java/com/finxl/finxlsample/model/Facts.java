package com.finxl.finxlsample.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 * Model class to encapsulate the data about the country coming from the Json.
 */
public class Facts {
    @Expose
    @SerializedName("title")
    private String title = null;
    @Expose
    @SerializedName("rows")
    private List<Fact> facts = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }
}
