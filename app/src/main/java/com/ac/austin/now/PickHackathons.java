package com.ac.austin.now;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import com.parse.ParseObject;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class PickHackathons extends Activity
{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    private ListView hackathonssListView;
    private ArrayList<Hackathon> hackathonsList = new ArrayList();

    private HackathonListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hackathon);

        hackathonssListView = (ListView) findViewById(R.id.list_hackathons);

        new RequestTask().execute("https://www.kimonolabs.com/api/46vt6zfe?apikey=2f216d8197b225ceca71483475e467f9");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshHackathonsList()
    {
        adapter = new HackathonListAdapter(this, R.layout.hackathon_list_item_layout, hackathonsList);
        hackathonssListView.setAdapter(adapter);
        hackathonssListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                ParseObject eventSubscription = new ParseObject("UserEvents3");
                eventSubscription.put("eventType", "hackathon");
                eventSubscription.put("name", hackathonsList.get(position)._name);
                eventSubscription.put("time", hackathonsList.get(position)._time);
                eventSubscription.put("place", hackathonsList.get(position)._place);
                eventSubscription.put("imageUrl", hackathonsList.get(position)._iconUrl);
                eventSubscription.saveInBackground();
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
                    JSONArray hackathons = jsonResponse.getJSONObject("results").getJSONArray("collection1");

                    hackathonsList.addAll(processJSON(hackathons));

                    // update the UI
                    refreshHackathonsList();
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

    public ArrayList<Hackathon> processJSON(JSONArray hackathons) throws JSONException
    {
        Hackathon hackathonObject;
        ArrayList<Hackathon> returnList = new ArrayList<Hackathon>();
        for (int i = 0; i < hackathons.length()/2; i++)
        {
            JSONObject hackathon = hackathons.getJSONObject(i);
            String name = (hackathon.getJSONObject("Title").getString("text"));
            String time = (hackathon.getJSONObject("time").getString("text"));
            String place = (hackathon.getJSONObject("place").getString("text"));
            String iconUrl;
            if(hackathon.getJSONArray("icon").length()>1){
                iconUrl = hackathon.getJSONArray("icon").getJSONObject(1).getString("src");
            }
            else{
                iconUrl = null;
            }
            if(iconUrl!=null){
                hackathonObject = new Hackathon(iconUrl, name, time, place);
                returnList.add(hackathonObject);
            }
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
