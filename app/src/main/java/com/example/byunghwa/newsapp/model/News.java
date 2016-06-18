package com.example.byunghwa.newsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ByungHwa on 6/16/2016.
 */
public class News implements Parcelable {

    private int id;
    private String url;
    private String title;
    private String contentSnippet;
    private String link;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentSnippet() {
        return this.contentSnippet;
    }

    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    private News(Parcel in) {
        url = in.readString();
        title = in.readString();
        contentSnippet = in.readString();
        link = in.readString();
    }

    public News () {

    }

    public static final Parcelable.Creator<News> CREATOR
            = new Parcelable.Creator<News>() {
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        public News[] newArray(int size) {
            return new News[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(contentSnippet);
        dest.writeString(link);
    }
}
