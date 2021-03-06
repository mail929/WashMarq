package com.mail929.android.washmarq;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WashActivity extends AppCompatActivity
{
    Context c;

    static final String[] urls = {"Abbottsford-Hall.aspx", "Campus-Town-East-Basement.aspx", "Campus-Town-East-2nd-Floor.aspx", "Campus-Town-East-3rd-Floor.aspx", "Campus-Town-East-4th-Floor.aspx", "Campus-Town-West-2nd-Floor.aspx", "Campus-Town-West-3rd-Floor.aspx", "Campus-Town-West-4th-Floor.aspx", "Carpenter-Tower.aspx", "Cobeen-Hall.aspx",
            "Gilman-Building.aspx", "Humphrey-Hall-1st-Floor.aspx", "Humphrey-Hall-2nd-Floor.aspx", "Humphrey-Hall-3rd-Floor.aspx",  "Humphrey-Hall-4th-Floor.aspx", "Humphrey-Hall-5th-Floor.aspx", "Humphrey-Hall-6th-Floor.aspx", "Mashuda-Hall.aspx", "McCabe-Hall.aspx", "McCormick-Hall.aspx",
            "O'Donnell-Hall.aspx", "Schroeder-Hall.aspx", "Straz-Tower.aspx"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash);

        SharedPreferences prefs = getSharedPreferences("WASHMARQ", 0);
        String fave = prefs.getString("FAVE", "NONE");

        if(!fave.equals("NONE"))
        {
            Intent intent = new Intent(this, BuildingActivity.class);
            intent.putExtra("URL", fave);
            startActivity(intent);
        }

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem_building, R.id.building, urls)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.listitem_building, parent, false);
                }
                view = super.getView(position, convertView, parent);

                TextView name = (TextView) view.findViewById(R.id.building);
                String hall = urls[position].replace("-", " ").replace(".aspx", " ");
                name.setText(hall);

                ColorProgressBar colorBar = view.findViewById(R.id.colorbar);
                new BarCreator().execute(getContext(), colorBar, urls[position]);
                return view;
            }
        });
        c = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                Intent intent = new Intent(c, BuildingActivity.class);
                intent.putExtra("URL", urls[position]);
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                SharedPreferences prefs = getSharedPreferences("WASHMARQ", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("FAVE", WashActivity.urls[position]);
                Toast.makeText(c, urls[position].replace("-", " ").replace(".aspx", " ") + "set as favorite", Toast.LENGTH_SHORT).show();
                editor.commit();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the inbuilding; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class BarCreator extends AsyncTask<Object, Void, Void>
    {
        ColorProgressBar bar;
        String buildingUrl;
        Context c;

        int good = 0;
        int moderate = 0;
        int bad = 0;

        @Override
        protected Void doInBackground(Object... objects)
        {
            c = (Context) objects[0];
            bar = (ColorProgressBar) objects[1];
            buildingUrl = (String) objects[2];
            List<Machine> machines = new DataFetcher().doInBackground(buildingUrl, c);
            for (Machine m : machines)
            {
                final String status = m.status;
                if (status.equals("Available"))
                {
                    good++;
                }
                else if (status.equals("In use") || status.equals("Not online") || status.equals("Out of order"))
                {
                    bad++;
                }
                else if (status.equals("Almost done") || status.equals("End of cycle") || status.equals("Ready to start") || status.equals("Payment in progress"))
                {
                    moderate++;
                }
            }

            System.out.println("For " + buildingUrl + ": G-" + good + " M-" + moderate + " B-" + bad);
            WashActivity.this.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    bar.setProgress(good, moderate, bad);
                }
            });
            return null;
        }
    }
}
