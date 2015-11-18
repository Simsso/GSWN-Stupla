package com.timodenk.gswnstupla;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;

public class StuplaActivity extends AppCompatActivity {
    private int chosenElementId, chosenWeek;

    private String chosenElementName;

    private WebView wvStupla;

    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stupla);

        // web view settings
        this.wvStupla = (WebView) findViewById(R.id.wvStupla);
        wvStupla.getSettings().setLoadWithOverviewMode(true);
        wvStupla.getSettings().setUseWideViewPort(true); // zoom out
        wvStupla.getSettings().setBuiltInZoomControls(true); // allow to zoom in
        wvStupla.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (StuplaActivity.this.swipeRefreshLayout.isRefreshing() == false) {
                    StuplaActivity.this.swipeRefreshLayout.setRefreshing(true); // show loading information
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.equals("about:blank")) {
                    StuplaActivity.this.swipeRefreshLayout.setRefreshing(false); // hide loading information
                }
            }
        });

        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateTaskBarElementName();

                        // clear cache when the user refreshes manually
                        StuplaActivity.this.wvStupla.clearCache(true);
                        updateWebView();
                    }
                }
        );

        // read passed element id, defines chosen week, updates webview and action bar text
        initialize();

        ((StuplaApplication) getApplication()).firstStart = false;
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
            // next and previous week buttons allow the user to navigate through the weeks
            case R.id.next_week: // next week
                incrementChosenWeek();
                updateWebView();
                break;
            case R.id.previous_week: // previous week
                decrementChosenWeek();
                updateWebView();
                break;
            default:
                break;
        }

        // has to be called to keep the back button working
        return (super.onOptionsItemSelected(item));
    }

    private void initialize() {
        // set week to current week
        Calendar c = Calendar.getInstance();
        this.chosenWeek = c.get(Calendar.WEEK_OF_YEAR);

        // show next week on sunday
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            if (c.getFirstDayOfWeek() == Calendar.MONDAY) {
                incrementChosenWeek();
            }

            // show information that the user sees the next week
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.showing_next_week), Toast.LENGTH_LONG).show();
        }

        // read passed element id
        Intent intent = getIntent();
        this.chosenElementId = intent.getIntExtra(MainActivity.ELEMENT_ID_MESSAGE, 0);

        // updates the url of the web view
        updateWebView();

        updateTaskBarElementName();
    }

    private void updateTaskBarElementName() {
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


    private void updateWebView() {
        // clear the web view
        this.wvStupla.loadUrl("about:blank");

        // request the url in a separate thread to keep the ui responsive
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final String url = Server.getElementUrl(StuplaActivity.this.chosenElementId, StuplaActivity.this.chosenWeek);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // update web view url
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

    private void incrementChosenWeek() {
        int tmp = this.chosenWeek;
        tmp++;

        while (tmp > 52) {
            tmp -= 52;
        }

        this.chosenWeek = tmp;
    }

    private void decrementChosenWeek() {
        int tmp = this.chosenWeek;
        tmp--;

        while (tmp <= 0) {
            tmp += 52;
        }

        this.chosenWeek = tmp;
    }
}
