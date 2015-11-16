package com.example.jcardenal.dailyselfie;

import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.Serializable;

/**
 * Created by jcardenal on 15/11/2015.
 */
public class Selfie implements Serializable {

    private String title;
    private String path;

    public Selfie(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getURI() {

        return Uri.parse("file:" + path);
    }

    @Override
    public String toString() {
        return title+" "+path;
    }
}
