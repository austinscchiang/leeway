package com.ac.austin.now;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class PickDinners extends Activity
{
    YelpAPI _yelpApi = new YelpAPI("Oauth cred", "Oauth cred", "Oauth cred", "Oauth cred");

    private ListView dinnersListView;
    private ArrayList<Dinner> dinnersList = new ArrayList();

    private DinnerListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner);

        dinnersListView = (ListView) findViewById(R.id.list_dinners);

        new RequestTask().execute("http://api.yelp.com/v2/search?term=food&location=Waterloo");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshDinnersList()
    {
        adapter = new DinnerListAdapter(this, R.layout.dinner_list_item_layout, dinnersList);
        dinnersListView.setAdapter(adapter);
        dinnersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ParseQuery<ParseObject> eventId = ParseQuery.getQuery(getString(R.string.latest_id));
                ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_object));
                query.whereEqualTo("eventType", "dinner");
                query.whereEqualTo("name", dinnersList.get(position)._name);
                final int pos = position;
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e)
                    {
                        // Move this to when the user selects participate button (after toggle)
                        if (parseObject == null) {
                            final ParseObject eventSubscription = new ParseObject(getString(R.string.parse_object));
                            eventSubscription.put("eventType", "dinner");
                            eventSubscription.put("name", dinnersList.get(pos)._name);
                            eventSubscription.put("scoreIcon", dinnersList.get(pos)._scoreIcon);
                            eventSubscription.put("imageUrl", dinnersList.get(pos)._previewIcon);
                            eventSubscription.put("primary", dinnersList.get(pos)._place);
                            eventSubscription.put("secondary", "");
                            eventSubscription.put("votes", 1);
                            eventId.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e)
                                {
                                    // Move this to when the user selects participate button (after toggle)
                                    if (parseObject == null) {
                                        ParseObject eventId = new ParseObject(getString(R.string.latest_id));
                                        eventId.put("eventId", 1);
                                        eventId.saveInBackground();
                                        eventSubscription.put("eventId", 1);
                                        eventSubscription.saveInBackground();
                                        ((UserApplication) getApplication()).votedForEvent(1);
                                    }
                                    else {
                                        int num = parseObject.getInt("eventId");
                                        num++;
                                        parseObject.put("eventId", num);
                                        parseObject.saveInBackground();
                                        eventSubscription.put("eventId", num);
                                        eventSubscription.saveInBackground();
                                        ((UserApplication) getApplication()).votedForEvent(num);
                                    }
                                }
                            });
                        }
                        else {
                            parseObject.increment("votes");
                            parseObject.saveInBackground();
                        }

                        Toast.makeText(getApplicationContext(), "Added \""+dinnersList.get(pos)._name+"\"  to Event Voting Pool", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
        // make a request to the specified url
        @Override
        protected String doInBackground(String... uri)
        {
            return _yelpApi.searchForBusinessesByLocation("restaurants", "Waterloo");
        }

        // if the request above completed successfully, this method will
        // automatically run so you can do something with the response
        @Override
        protected void onPostExecute(String response)
        {
            super.onPostExecute(response);

            if (response != null)
            {
                try
                {
                    // convert the String response to a JSON object,
                    // because JSON is the response format Rotten Tomatoes uses
                    JSONObject jsonResponse = new JSONObject(response);

                    // fetch the array of dinners in the response
                    JSONArray dinners = jsonResponse.getJSONArray("businesses");

                    dinnersList.addAll(processJSON(dinners));

                    // update the UI
                    refreshDinnersList();
                }
                catch (JSONException e)
                {
                    Log.d("Test", "Failed to parse the JSON response!");
                }
            }
        }
    }

    public ArrayList<Dinner> processJSON(JSONArray dinners) throws JSONException
    {
        Dinner dinnerObject;
        ArrayList<Dinner> returnList = new ArrayList<Dinner>();
        for (int i = 0; i < dinners.length(); i++)
        {
            JSONObject dinner = dinners.getJSONObject(i);
            String name = (dinner.getString("name"));
            String id = (dinner.getString("id"));
            String scoreIcon = (dinner.getString("rating_img_url_large"));
            String previewIcon = (dinner.getString("image_url"));
            String place = (dinner.getJSONObject("location").getJSONArray("address").getString(0));
            dinnerObject = new Dinner(name, id, scoreIcon, previewIcon, place);
            returnList.add(dinnerObject);
        }
        return returnList;
    }

    public String decompress(byte[] compressed) throws IOException
    {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

}
