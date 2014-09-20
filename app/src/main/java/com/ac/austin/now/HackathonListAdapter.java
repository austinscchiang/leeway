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
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class HackathonListAdapter extends ArrayAdapter{
    private LayoutInflater _inflater;
    private int _layout;
    private ArrayList<Hackathon> _entries;


    public HackathonListAdapter(Activity a, int textViewResourceId, ArrayList<Hackathon> entries) {
        super(a, textViewResourceId, entries);
        _layout = textViewResourceId;
        _entries = entries;
        _inflater = LayoutInflater.from(a);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Hackathon hackathon = _entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = _inflater.inflate(_layout, parent, false);
        }
        TextView hackathonNameView = (TextView)v.findViewById(R.id.hackathon_name);
        TextView hackathonDescriptionView = (TextView)v.findViewById(R.id.hackathon_description);
        ImageView hackathonThumbnailView = (ImageView)v.findViewById(R.id.hackathon_thumbnail);

        hackathonNameView.setText(hackathon._name);
        hackathonDescriptionView.setText(hackathon._place + " | " + hackathon._time);
        new LoadImageTask(hackathonThumbnailView)
                .execute(hackathon._iconUrl);
        return v;

    }

//    public static Bitmap loadBitmap(String url) {
//        try{
//            URL newurl = new URL(url);
//            Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
//            return mIcon_val;
//        }
//        catch(Exception e) {
//            Log.d("Bitmap", "Bitmap failed to load");
//            return null;
//
//        }
//    }

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
