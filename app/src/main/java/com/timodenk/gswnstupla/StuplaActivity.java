package com.timodenk.gswnstupla;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ArrayAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;

public class StuplaActivity extends AppCompatActivity {
    private int chosenElementId, chosenWeek;

    private String chosenElementName;

    private WebView wvStupla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stupla);

        // web view settings
        this.wvStupla = (WebView) findViewById(R.id.wvStupla);
        wvStupla.getSettings().setLoadWithOverviewMode(true);
        wvStupla.getSettings().setUseWideViewPort(true);
        wvStupla.getSettings().setBuiltInZoomControls(true);


        // set week to current week
        Calendar c = Calendar.getInstance();
        this.chosenWeek = c.get(Calendar.WEEK_OF_YEAR);

        // read passed element id
        Intent intent = getIntent();
        this.chosenElementId = intent.getIntExtra(MainActivity.ELEMENT_ID_MESSAGE, 0);

        // updates the url of the web view
        updateWebView();

        // download the name of the chosen element
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    StuplaActivity.this.chosenElementName = Server.getElementName(StuplaActivity.this.chosenElementId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(StuplaActivity.this.chosenElementName);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stupla, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next_week:
                this.chosenWeek++;
                updateWebView();
                break;
            // action with ID action_settings was selected
            case R.id.previous_week:
                this.chosenWeek--;
                updateWebView();
                break;
            default:
                break;
        }

        return (super.onOptionsItemSelected(item));
    }


    private void updateWebView() {
        this.wvStupla.loadUrl("about:blank");
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final String url = Server.getElementUrl(StuplaActivity.this.chosenElementId, StuplaActivity.this.chosenWeek);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StuplaActivity.this.wvStupla.loadUrl(url);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
