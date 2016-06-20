package com.example.byunghwa.newsapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ByungHwa on 6/16/2016.
 */
public class News implements Parcelable {

    private int id;
    private String title;
    private String pubDate;
    private String link;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return this.pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    private News(Parcel in) {
        title = in.readString();
        link = in.readString();
        pubDate = in.readString();
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
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(pubDate);
    }
}
