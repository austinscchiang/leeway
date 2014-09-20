package com.ac.austin.now;

import android.util.EventLogTags;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class Concert {
    public String _name;
    public String _time;
    public String _place;
    public String _iconUrl;

    public Concert(String name, String time, String place, String url){
        _name = name;
        _time = time;
        _place = place;
        _iconUrl = url;
    }

}
