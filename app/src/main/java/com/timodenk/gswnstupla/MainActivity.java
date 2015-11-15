package com.timodenk.gswnstupla;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.timodenk.json.*;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String ELEMENT_ID_MESSAGE = "com.timodenk.gswnstupla.ELEMENT_ID_MESSAGE", PREFS_NAME = "GSWN_Stupla_Preferences";

    ListView lvElement;

    public static String[] elements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.lvElement = (ListView) findViewById(R.id.lvElements);

        // show logo in action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.icon);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        fetchAndShowElementList();

        // listen for click on list element
        this.lvElement.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showStupla(position + 1);
            }
        });

        // check whether a stored element id is available
        int savedElementId = LocalStorage.loadElementId(this);
        if (((StuplaApplication)getApplication()).firstStart && savedElementId >= 0) {
            // if it is show the stupla of the id
            showStupla(savedElementId);
        }
        ((StuplaApplication)getApplication()).firstStart = false;
    }

    private void fetchAndShowElementList() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    // fetch array of elements
                    MainActivity.elements = Server.getElementNames();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // link array of element names to the list view
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    MainActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    MainActivity.elements);
                            MainActivity.this.lvElement.setAdapter(arrayAdapter);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // server request in a separate thread to keep the ui responsive
        thread.start();
    }

    private void showStupla(int elementId) {
        // save which element the user uses
        LocalStorage.saveElementId(this, elementId);

        Intent intent = new Intent(MainActivity.this, StuplaActivity.class);
        // pass the element id to the stupla activity
        intent.putExtra(ELEMENT_ID_MESSAGE, elementId);
        // start stupla activity
        startActivity(intent);
    }
}
