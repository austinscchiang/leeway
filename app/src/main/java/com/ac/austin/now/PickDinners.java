package com.ac.austin.now;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class PickDinners extends Activity {
    YelpAPI _yelpApi = new YelpAPI("LqeDHctSMkc3KeV7qy8gLQ", "HhUV2dwiW35MDk68twXP62tUdXk", "p-2jK2LGuo0ItJPCiKCHRYagRCei2gen", "2BOEeKWpyBHSlvS8Jm00Quh-FTE");

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
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // make event
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
            String scoreIcon = (dinner.getString("rating_img_url"));
            String previewIcon = (dinner.getString("snippet_image_url"));
            String place = (dinner.getJSONObject("location").getJSONArray("address").getString(0));
            dinnerObject = new Dinner(name, scoreIcon, previewIcon, place);
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