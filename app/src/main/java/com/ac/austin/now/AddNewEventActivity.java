package com.ac.austin.now;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by austinchiang on 2014-09-20.
 */
public class AddNewEventActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        View foodView = findViewById(R.id.food_view);
        View concertView = findViewById(R.id.concert_view);
        View movieView = findViewById(R.id.movie_view);
        View hackathonView = findViewById(R.id.hack_view);

        foodView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), PickDinners.class);
                startActivity(intent);
            }
        });
        concertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), PickConcerts.class);
                startActivity(intent);
            }
        });
        movieView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), PickMovies.class);
                startActivity(intent);
            }
        });
        hackathonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), PickHackathons.class);
                startActivity(intent);
            }
        });
    }

}
