package com.ac.austin.now;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.Parse;
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
//        if
        if (id == R.id.new_dinner) {
            Intent intent = new Intent(getApplicationContext(), PickDinners.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.new_concert) {
            Intent intent = new Intent(getApplicationContext(), PickConcerts.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.new_movie) {
            Intent intent = new Intent(getApplicationContext(), PickMovies.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.new_hack) {
            Intent intent = new Intent(getApplicationContext(), PickHackathons.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);
        Parse.initialize(this, "ZeUV6QK4coSIVkUSi71m96vIbANaQeNEbvtj3klI", "90HC87BRscSq8YT9ZjVahVtVIyncYuRN382QKTaW");

        eventsListView = (ListView) findViewById(R.id.list_events);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        refresh();
    }

    public void refresh()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_object));
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e)
            {
//                ParseObject.deleteAllInBackground(parseObjects, null);
                if (e == null) {
                    eventsList.clear();
                    for (ParseObject data : parseObjects) {
//                        data.deleteEventually();

                        eventsList.add(new UserEvent(data.getString("name"), data.getString("primary"), data.getString("imageUrl"), data.getString("secondary"), data.getInt("eventId"), data.getInt("votes")));

                    }
                    for (UserEvent event : eventsList) {
                        if (((UserApplication) getApplication()).hasVoted(event._id)) {
                            event._hasVoted = true;
                        }
                        else {
                            event._hasVoted = false;
                        }
                    }
                    refreshEventsList();
                }
            }
        });
    }

    private void toggleContents(View view)
    {
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

}
