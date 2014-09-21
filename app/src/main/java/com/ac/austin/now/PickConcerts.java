package com.ac.austin.now;

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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class PickConcerts extends Activity{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    private ListView concertsListView;
    private ArrayList<Concert> concertsList = new ArrayList();

    private ConcertListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concert);

        concertsListView = (ListView) findViewById(R.id.list_concerts);

        new RequestTask().execute("https://www.kimonolabs.com/api/cc5sjff4?apikey=2f216d8197b225ceca71483475e467f9");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshConcertsList()
    {
        adapter = new ConcertListAdapter(this, R.layout.concert_list_item_layout, concertsList);
        concertsListView.setAdapter(adapter);
        concertsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_object));
                final ParseQuery<ParseObject> eventId = ParseQuery.getQuery(getString(R.string.latest_id));
                query.whereEqualTo("eventType", "concert");
                query.whereEqualTo("name", concertsList.get(position)._name);
                query.whereEqualTo("primary", concertsList.get(position)._time);
                query.whereEqualTo("secondary", concertsList.get(position)._place);
                final int pos = position;
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        // Move this to when the user selects participate button (after toggle)
                        if (parseObject == null) {
                            final ParseObject eventSubscription = new ParseObject(getString(R.string.parse_object));
                            eventSubscription.put("eventType", "concert");
                            eventSubscription.put("name", concertsList.get(pos)._name);
                            eventSubscription.put("primary", concertsList.get(pos)._time);
                            eventSubscription.put("secondary", concertsList.get(pos)._place);
                            eventSubscription.put("imageUrl", concertsList.get(pos)._iconUrl);
                            eventSubscription.put("votes", 1);
                            eventId.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e)
                                {
                                    // Move this to when the user selects participate button (after toggle)
                                    if (parseObject == null) {
                                        ParseObject eventId = new ParseObject(getString(R.string.latest_id));
                                        eventId.put("id", 1);
                                        eventSubscription.put("id", 1);
                                        eventId.saveInBackground();
                                        ((UserApplication) getApplication()).votedForEvent(1);
                                    }
                                    else {
                                        parseObject.increment("id");
                                        parseObject.saveInBackground();
                                        eventSubscription.put("id", parseObject.getInt("id"));
                                        ((UserApplication) getApplication()).votedForEvent(parseObject.getInt("id"));
                                    }
                                }
                            });

                            eventSubscription.saveInBackground();
                        } else {
                            parseObject.increment("votes");
                            parseObject.saveInBackground();
                        }
                    }
                });

                Toast.makeText(getApplicationContext(), "Added \""+concertsList.get(pos)._name+"\"to Event Voting Pool", Toast.LENGTH_SHORT).show();

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

                    // fetch the array of concerts in the response
                    JSONArray concerts = jsonResponse.getJSONObject("results").getJSONArray("collection1");

                    concertsList.addAll(processJSON(concerts));

                    // update the UI
                    refreshConcertsList();
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

    public ArrayList<Concert> processJSON(JSONArray concerts) throws JSONException
    {
        Concert concertObject;
        ArrayList<Concert> returnList = new ArrayList<Concert>();
        for (int i = 0; i < concerts.length(); i++)
        {
            JSONObject concert = concerts.getJSONObject(i);
            String name = (concert.getJSONObject("name").getString("text"));
            String time = (concert.getString("time"));
            String place = (concert.getJSONObject("place").getString("text"));
            String icon = (concert.getJSONObject("icon").getString("src"));
            concertObject = new Concert(name, time, place, icon);
            returnList.add(concertObject);
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
