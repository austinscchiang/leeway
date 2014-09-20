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

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class ConcertListAdapter extends ArrayAdapter {
    private LayoutInflater _inflater;
    private int _layout;
    private ArrayList<Concert> _entries;


    public ConcertListAdapter(Activity a, int textViewResourceId, ArrayList<Concert> entries) {
        super(a, textViewResourceId, entries);
        _layout = textViewResourceId;
        _entries = entries;
        _inflater = LayoutInflater.from(a);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Concert concert = _entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = _inflater.inflate(_layout, parent, false);
        }
        TextView concertNameView = (TextView)v.findViewById(R.id.concert_name);
        TextView concertTimeView = (TextView)v.findViewById(R.id.concert_time);
        TextView concertPlaceView = (TextView)v.findViewById(R.id.concert_place);
        ImageView concertThumbnailView = (ImageView)v.findViewById(R.id.concert_thumbnail);

        concertNameView.setText(concert._name);
        concertTimeView.setText(concert._time);
        concertPlaceView.setText(concert._place);
        new LoadImageTask(concertThumbnailView)
                .execute(concert._iconUrl);
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
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }
}
