package com.ac.austin.now;

import android.app.Application;

import java.util.HashSet;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class UserApplication extends Application
{
    public HashSet<Integer> votedEvents = new HashSet<Integer>();

    public boolean hasVoted(int eventId)
    {
        return votedEvents.contains(eventId);
    }

    public void votedForEvent(int eventId)
    {
        votedEvents.add(eventId);
    }
}
