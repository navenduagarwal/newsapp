package com.example.navendu.newsapp;

/**
 * Created by navendu on 7/2/2016.
 */
public class News {
    private String title;
    private String author;
    private String thumbnail;
    private String newsURL;


    public News(String title, String author, String thumbnail, String newsURL) {
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
        this.newsURL = newsURL;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getNewsURL() {
        return newsURL;
    }

}
