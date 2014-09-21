package com.ac.austin.now;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class Movie {
    public String _title;
    public int _id;
    public int _criticsRating;
    public int _audienceRating;
    public String _mpaaRating;
    public String _imageUrl;
    public Movie (String title, int id, int criticsRating, int audienceRating, String mpaaRating, String imageUrl){
        _title = title;
        _id = id;
        _criticsRating = criticsRating;
        _audienceRating = audienceRating;
        _mpaaRating = mpaaRating;
        _imageUrl = imageUrl;
    }
}
