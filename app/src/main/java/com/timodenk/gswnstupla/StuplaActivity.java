package com.timodenk.gswnstupla;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
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

    private int[] availableWeeks = null;

    private String chosenElementName;

    private WebView wvStupla;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Menu stuplaMenu;
    private MenuItem nextWeek = null, previousWeek = null;


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


        // allow swipe down to reload element web page
        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateTaskBarElementName();

                        // clear cache when the user refreshes manually
                        StuplaActivity.this.wvStupla.clearCache(true);
                        updateWebView();

                        downloadAvailableWeeks();
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

        this.stuplaMenu = menu;
        this.nextWeek = this.stuplaMenu.findItem(R.id.next_week);
        this.previousWeek = this.stuplaMenu.findItem(R.id.previous_week);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // next and previous week buttons allow the user to navigate through the weeks
            case R.id.next_week: // next week
                if (incrementChosenWeek()) {
                    updateWebView();
                }
                updateChangeWeekButtons();
                break;
            case R.id.previous_week: // previous week
                if (decrementChosenWeek()) {
                    updateWebView();
                }
                updateChangeWeekButtons();
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

        downloadAvailableWeeks();
    }

    private void downloadAvailableWeeks() {
        // download the available weeks
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    StuplaActivity.this.availableWeeks = Server.getAvailableWeeks();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateChangeWeekButtons();
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

    private boolean incrementChosenWeek() {
        int tmp = this.chosenWeek;
        tmp++;

        while (tmp > 52) {
            tmp -= 52;
        }

        if (incrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    private boolean decrementChosenWeek() {
        int tmp = this.chosenWeek;
        tmp--;

        while (tmp <= 0) {
            tmp += 52;
        }

        if (decrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    private boolean incrementWeekAvailable(int newWeek) {
        return (this.availableWeeks == null || weekAvailable(newWeek) || this.availableWeeks[0] > newWeek);
    }

    private boolean decrementWeekAvailable(int newWeek) {
        return (this.availableWeeks == null || weekAvailable(newWeek) || this.availableWeeks[this.availableWeeks.length - 1] < newWeek);
    }


    private boolean weekAvailable(int week) {
        if (this.availableWeeks == null) {
            return true;
        }

        for (int i = 0; i < this.availableWeeks.length; i++) {
            if (this.availableWeeks[i] == week) {
                return true;
            }
        }
        return false;
    }

    private void updateChangeWeekButtons() {
        // next week button
        boolean incrementAvailable = incrementWeekAvailable(this.chosenWeek + 1);
        this.nextWeek.setEnabled(incrementAvailable);
        this.nextWeek.setIcon(incrementAvailable ? ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_right_white_48dp) : ContextCompat.getDrawable(this, R.drawable.ic_transparent_48dp));

        // previous week button
        boolean decrementAvailable = decrementWeekAvailable(this.chosenWeek - 1);
        this.previousWeek.setEnabled(decrementAvailable);
        this.previousWeek.setIcon(decrementAvailable ? ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_left_white_48dp) : ContextCompat.getDrawable(this, R.drawable.ic_transparent_48dp));
    }
}
