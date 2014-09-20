package com.ac.austin.now;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.*;
import com.facebook.widget.LoginButton;

public class MainActivity extends Activity
{
    ArrayList<EventType> _eventTypeList = new ArrayList<EventType>();

    EventTypeListAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, "cXbkz9olUPkokRVGGrES8bCUiNGoyCMS6F7FrKfD", "VaAJmOuV2t91IeYJOx2WERrGSUbkpvRcCiK66d8q");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ArrayList<String> _eventNameList = new ArrayList<String>(Arrays.asList("Movies", "Concerts", "Dinner", "Hackathons", "Sporting Events"));
        ListView eventList = (ListView)findViewById(R.id.event_type_list);
        _eventTypeList.clear();
        for (int i = 0; i < _eventNameList.size(); i++) {
            _eventTypeList.add(new EventType(_eventNameList.get(i), i));
        }

        _adapter = new EventTypeListAdapter(this, R.layout.event_type_list_item_layout, _eventTypeList);
        eventList.setAdapter(_adapter);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position){
//                    // movies
                    case 0:
                        intent = new Intent(getApplicationContext(), PickMovies.class);
                        break;
                    case 1:
                        intent = new Intent(getApplicationContext(), PickConcerts.class);
                        break;
                    case 2:
                        intent = new Intent(getApplicationContext(), PickDinners.class);
                        break;
                    case 3:
                        intent = new Intent(getApplicationContext(), PickHackathons.class);
                        break;
                    default:
                        intent = new Intent(getApplicationContext(), PickUserEvents.class);
                        break;
                }

                startActivity(intent);
            }
        });



    }

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
        if (id == R.id.a) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
