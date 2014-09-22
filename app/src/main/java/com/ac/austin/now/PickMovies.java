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
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class PickMovies extends Activity
{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "Key";

    private ListView moviesListView;
    private ArrayList<Movie> moviesList = new ArrayList();

    private MovieListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        moviesListView = (ListView) findViewById(R.id.list_movies);

        new RequestTask().execute("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=15&page=1&country=us&apikey=key");

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

    }

    private void refreshMoviesList()
    {
        adapter = new MovieListAdapter(this, R.layout.movie_list_item_layout, moviesList);
        moviesListView.setAdapter(adapter);
        moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                final ParseQuery<ParseObject> eventId = ParseQuery.getQuery(getString(R.string.latest_id));
                ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_object));
                query.whereEqualTo("eventType", "movie");
                query.whereEqualTo("name", moviesList.get(position)._title);
                final int pos = position;
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        // Move this to when the user selects participate button (after toggle)
                        if (parseObject == null) {
                            final ParseObject eventSubscription = new ParseObject(getString(R.string.parse_object));
                            eventSubscription.put("eventType", "movie");
                            eventSubscription.put("name", moviesList.get(pos)._title);
                            String rating = "Critics: " + moviesList.get(pos)._criticsRating + "% | " + "Audience: " + moviesList.get(pos)._audienceRating + "% | " + "Rated: " + moviesList.get(pos)._mpaaRating;
                            eventSubscription.put("primary", rating);
                            eventSubscription.put("secondary", "");
                            eventSubscription.put("imageUrl", moviesList.get(pos)._imageUrl);
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
                        } else {
                            parseObject.increment("votes");
                            parseObject.saveInBackground();
                        }
                    }
                });
                Toast.makeText(getApplicationContext(), "Added \""+moviesList.get(pos)._title+"\" to Event Voting Pool", Toast.LENGTH_SHORT).show();


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
                    JSONArray movies = jsonResponse.getJSONArray("movies");

                    moviesList.addAll(processJSON(movies));

                    // update the UI
                    refreshMoviesList();
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

    public ArrayList<Movie> processJSON(JSONArray movies) throws JSONException
    {
        Movie movieObject;
        ArrayList<Movie> returnList = new ArrayList<Movie>();
        for (int i = 0; i < movies.length(); i++)
        {
            JSONObject movie = movies.getJSONObject(i);
            String title = (movie.getString("title"));
            int id = (Integer.parseInt(movie.getString("id")));
            int criticsRating = (Integer.parseInt(movie.getJSONObject("ratings").getString("critics_score")));
            int audienceRating = (Integer.parseInt(movie.getJSONObject("ratings").getString("audience_score")));
            String mpaaRating = (movie.getString("mpaa_rating"));
            String imageUrl = (movie.getJSONObject("posters").getString("profile"));
            movieObject = new Movie(title, id, criticsRating, audienceRating, mpaaRating, imageUrl);
            returnList.add(movieObject);
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
