package com.mail929.android.washmarq;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mail929 on 1/26/16.
 */
public class BuildingActivity extends AppCompatActivity
{
    ArrayList<Machine> machines;
    LinearLayout list;
    String url = "straz-tower.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        Intent intent = getIntent();
        url = intent.getExtras().getString("URL");

        getSupportActionBar().setTitle(url.replace("-", " ").replace(".aspx", " "));

        machines = DataFetcher.machineList;
        list = (LinearLayout) findViewById(R.id.list);

        DataFetcher.fetchData(url);
        machines = DataFetcher.machineList;
        updateList();

        (new Thread()
        {
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Thread.sleep(15 * 1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println("Refreshing");
                    DataFetcher.fetchData(url);
                    machines = DataFetcher.machineList;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateList();
                        }
                    });
                }
            }
        }).start();
    }

    public void updateList()
    {
        System.out.println("Updating list");
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < machines.size(); i++)
        {
            View view = inflater.inflate(android.R.layout.simple_list_item_2, list, false);

            Machine m = machines.get(i);
            String status = m.status;
            String name = m.name;
            String type = m.type;
            int time = m.time;

            TextView nametv = (TextView) view.findViewById(android.R.id.text1);
            nametv.setText(type + " " + name);

            TextView statustv = (TextView) view.findViewById(android.R.id.text2);
            if (time > 0)
            {
                statustv.setText(status + " - " + time + " minutes remaining");
            } else
            {
                statustv.setText(status);
            }

            if (status.equals("Available"))
            {
                view.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else if (status.equals("In use") || status.equals("Not online") || status.equals("Out of order"))
            {
                view.setBackgroundColor(Color.parseColor("#F44336"));
            } else if (status.equals("Almost done") || status.equals("End of cycle") || status.equals("Ready to start") || status.equals("Payment in progress"))
            {
                view.setBackgroundColor(Color.parseColor("#FFEB3B"));
            }
            list.addView(view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                DataFetcher.fetchData(url);
                machines = DataFetcher.machineList;
                updateList();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
