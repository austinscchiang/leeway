package com.ac.austin.now;

/**
 * Created by nitarshanr on 14-09-20.
 */
public class UserEvent {
    public String _name;
    public String _primary;
    public String _imageUrl;
    public String _secondary;
    public UserEvent (String name, String subtitle, String imageUrl, String secondary) {
        _name = name;
        _primary = subtitle;
        _imageUrl = imageUrl;
        _secondary = secondary;
    }
}
