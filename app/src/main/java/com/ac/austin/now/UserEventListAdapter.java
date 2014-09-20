package com.ac.austin.now;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class UserEventListAdapter extends ArrayAdapter {
    private LayoutInflater _inflater;
    private int _layout;
    private ArrayList<UserEvent> _entries;

    private TextView _eventTypeTextView;

    public UserEventListAdapter(Activity a, int textViewResourceId, ArrayList<UserEvent> entries) {
        super(a, textViewResourceId, entries);
        _layout = textViewResourceId;
        _entries = entries;
        _inflater = LayoutInflater.from(a);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserEvent event = _entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = _inflater.inflate(_layout, parent, false);
        }
        TextView eventTitleView = (TextView)v.findViewById(R.id.event_title);
        TextView eventPrimaryView = (TextView)v.findViewById(R.id.event_primary);
        TextView eventSecondaryView = (TextView)v.findViewById(R.id.event_secondary);
        ImageView eventThumbnailView = (ImageView)v.findViewById(R.id.event_thumbnail);

        eventTitleView.setText(event._name);
        eventPrimaryView.setText(event._primary);
        if(event._secondary != null && !event._secondary.equals("")){
            eventSecondaryView.setText(event._secondary);
        }
        new LoadImageTask(eventThumbnailView)
                .execute(event._imageUrl);
        return v;

    }

    class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public LoadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }
}
