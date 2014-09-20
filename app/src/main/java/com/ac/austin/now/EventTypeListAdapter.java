package com.ac.austin.now;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class EventTypeListAdapter extends ArrayAdapter {
    private LayoutInflater _inflater;
    private int _layout;
    private ArrayList<EventType> _entries;

    private TextView _eventTypeTextView;

    public EventTypeListAdapter(Activity a, int textViewResourceId, ArrayList<EventType> entries) {
        super(a, textViewResourceId, entries);
        _layout = textViewResourceId;
        _entries = entries;
        _inflater = LayoutInflater.from(a);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EventType eventType = _entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = _inflater.inflate(_layout, parent, false);
        }
        _eventTypeTextView = (TextView) v.findViewById(R.id.event_type_text);
        _eventTypeTextView.setText(eventType._eventTypeName);
        return v;
    }
}
