package com.example.myplaces.models;
import android.location.Location;
import android.net.Uri;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable {

    private String title;
    private Location location;
    private Date date;
    private String description;
    private boolean isHeart;
    private Uri imageLoc;

    public Note() {
        setTitle("");
        setDescription("");
        setImageLoc(Uri.EMPTY);
        setHeart(false);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHeart() {
        return isHeart;
    }

    public void setHeart(boolean heart) {
        isHeart = heart;
    }

    public Uri getImageLoc() {
        return imageLoc;
    }

    public void setImageLoc(Uri imageLoc) {
        this.imageLoc = imageLoc;
    }
}
