package com.ac.austin.now;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class PickUserEvents extends Activity
{
    private ListView eventsListView;
    private ArrayList<UserEvent> eventsList = new ArrayList();
    private ArrayList<String> friendList = new ArrayList();

    private UserEventListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);

        eventsListView = (ListView) findViewById(R.id.list_events);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserEvents3");
        //query.whereContainedIn("userId", Arrays.asList(friendList));
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject data : parseObjects) {
                        eventsList.add(new UserEvent(data.getString("name"), data.getString("subtitle"), data.getString("imageUrl")));
                    }
                    refreshEventsList();
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void toggleContents(View view) {
    // show selection/dismissal options and more info
    }

    private void refreshEventsList()
    {
        adapter = new UserEventListAdapter(this, R.layout.user_events_list_item_layout, eventsList);
        eventsListView.setAdapter(adapter);
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                toggleContents(view);
            }
        });

    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
        // make a request to the specified url
        @Override
        protected String doInBackground(String... uri)
        {
            return launchHTTPRequest(uri[0]);
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

                    // fetch the array of movies in the response
                    JSONArray events = jsonResponse.getJSONArray("movies");

                    eventsList.addAll(processJSON(events));

                    // update the UI
                    refreshEventsList();
                }
                catch (JSONException e)
                {
                    Log.d("Test", "Failed to parse the JSON response!");
                }
            }
        }
    }

    public String launchHTTPRequest(String uri)
    {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;

        boolean isGzip = false;
        try
        {
            // make a HTTP request
            response = httpclient.execute(new HttpGet(uri));
            StatusLine statusLine = response.getStatusLine();

            if (response.getFirstHeader("Content-Encoding") != null) {
                isGzip = true;
                Log.d("GZIP", "Stream is gzipped");
            }
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                // request successful - read the response and close the connection
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                if (isGzip) {
                    responseString = decompress(out.toByteArray());
                }
                else {
                    responseString = out.toString();
                }
            }
            else
            {
                // request failed - close the connection
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        }
        catch (Exception e)
        {
            Log.d("Test", "Couldn't make a successful request!");
        }
        return responseString;
    }

    public ArrayList<UserEvent> processJSON(JSONArray movies) throws JSONException
    {
        UserEvent movieObject;
        ArrayList<UserEvent> returnList = new ArrayList<UserEvent>();
        for (int i = 0; i < movies.length(); i++)
        {
            JSONObject movie = movies.getJSONObject(i);
            String title = (movie.getString("title"));
            int id = (Integer.parseInt(movie.getString("id")));
            int criticsRating = (Integer.parseInt(movie.getJSONObject("ratings").getString("critics_score")));
            int audienceRating = (Integer.parseInt(movie.getJSONObject("ratings").getString("audience_score")));
            String mpaaRating = (movie.getString("mpaa_rating"));
            String imageUrl = (movie.getJSONObject("posters").getString("profile"));
            //movieObject = new Movie(title, id, criticsRating, audienceRating, mpaaRating, imageUrl);
            //returnList.add(movieObject);
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
