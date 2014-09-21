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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    private int plus;
    private int check;
    private Activity _activity;

    private TextView _eventTypeTextView;

    public UserEventListAdapter(Activity a, int textViewResourceId, ArrayList<UserEvent> entries) {
        super(a, textViewResourceId, entries);
        _layout = textViewResourceId;
        _entries = entries;
        _inflater = LayoutInflater.from(a);
        _activity = a;

        plus = a.getResources().getIdentifier("@drawable/plus_icon", null, a.getPackageName());
        check = a.getResources().getIdentifier("@drawable/checkmark_icon", null, a.getPackageName());
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserEvent event = _entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = _inflater.inflate(_layout, parent, false);
        }
        TextView eventTitleView = (TextView)v.findViewById(R.id.event_title);
        TextView eventPrimaryView = (TextView)v.findViewById(R.id.event_primary);
        TextView eventSecondaryView = (TextView)v.findViewById(R.id.event_secondary);
        final TextView eventFriendView = (TextView)v.findViewById(R.id.event_friends);
        ImageView eventThumbnailView = (ImageView)v.findViewById(R.id.event_thumbnail);
        final ImageView eventAddView = (ImageView)v.findViewById(R.id.event_add_icon);

        if(event._hasVoted){
            eventAddView.setImageResource(check);
        }
        else{
            eventAddView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event._hasVoted = true;
                    ((UserApplication) _activity.getApplication()).votedForEvent(event._id);
                    event._votes++;
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(_activity.getString(R.string.parse_object));
                    query.whereEqualTo("eventId", event._id);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            // Move this to when the user selects participate button (after toggle)
                            String name = parseObject.getString("name");
                            if (parseObject != null) {
                                int num = event._votes;
                                parseObject.put("votes", num);
                                parseObject.saveInBackground();
                            }
                        }
                    });
                    eventFriendView.setText(event._votes+((event._votes==1)?" friend is ":" friends are ") + " going.");
                    eventAddView.setImageResource(check);
                }
            });
        }
        eventTitleView.setText(event._name);
        eventPrimaryView.setText(event._primary);
        if(event._secondary != null && !event._secondary.equals("")){
            eventSecondaryView.setText(event._secondary);
            eventSecondaryView.setVisibility(View.VISIBLE);
        }
        eventFriendView.setText(event._votes+((event._votes==1)?" friend is ":" friends are ") + " going.");
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
