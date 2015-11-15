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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.icon);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        fetchAndShowElementList();

        this.lvElement.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showStupla(position + 1);
            }
        });

        int savedElementId = LocalStorage.loadElementId(this);
        if (((StuplaApplication)getApplication()).firstStart && savedElementId >= 0) {
            showStupla(savedElementId);
        }
        ((StuplaApplication)getApplication()).firstStart = false;
    }

    private void fetchAndShowElementList() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    MainActivity.elements = Server.getElementNames();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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

        thread.start();
    }

    private void showStupla(int elementId) {
        LocalStorage.saveElementId(this, elementId);

        Intent intent = new Intent(MainActivity.this, StuplaActivity.class);
        intent.putExtra(ELEMENT_ID_MESSAGE, elementId);
        startActivity(intent);
    }
}
