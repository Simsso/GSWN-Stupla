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


public class StuplaActivity extends AppCompatActivity {
    private StuplaControl control;

    public WebView wvStupla;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Menu stuplaMenu;
    public MenuItem nextWeek = null, previousWeek = null;


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
                if (swipeRefreshLayout.isRefreshing() == false) {
                    swipeRefreshLayout.setRefreshing(true); // show loading information
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.equals(StuplaControl.URL_ABOUT_BLANK)) {
                    swipeRefreshLayout.setRefreshing(false); // hide loading information
                }
            }
        });


        // allow swipe down to reload element web page
        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        control.updateTaskBarElementName();

                        // clear cache when the user refreshes manually
                        wvStupla.clearCache(true);
                        control.updateWebView();

                        control.downloadAvailableWeeks();
                    }
                }
        );

        ((StuplaApplication) getApplication()).firstStart = false;

        // read passed element id
        Intent intent = getIntent();
        int chosenElementId = intent.getIntExtra(MainActivity.ELEMENT_ID_MESSAGE, 0);

        this.control = new StuplaControl(this, chosenElementId);
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
                if (control.incrementChosenWeek()) {
                    control.updateWebView();
                }
                updateChangeWeekButtons();
                break;
            case R.id.previous_week: // previous week
                if (control.decrementChosenWeek()) {
                    control.updateWebView();
                }
                updateChangeWeekButtons();
                break;
            default:
                break;
        }

        // has to be called to keep the back button working
        return (super.onOptionsItemSelected(item));
    }

    public void showToast(int textResource) {
        Toast.makeText(getApplicationContext(), getResources().getString(textResource), Toast.LENGTH_LONG).show();
    }


    public void updateChangeWeekButtons() {
        // next week button
        boolean incrementAvailable = control.incrementWeekAvailable();
        nextWeek.setEnabled(incrementAvailable);
        nextWeek.setIcon(incrementAvailable ? ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_right_white_48dp) : ContextCompat.getDrawable(this, R.drawable.ic_transparent_48dp));

        // previous week button
        boolean decrementAvailable = control.decrementWeekAvailable();
        previousWeek.setEnabled(decrementAvailable);
        previousWeek.setIcon(decrementAvailable ? ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_left_white_48dp) : ContextCompat.getDrawable(this, R.drawable.ic_transparent_48dp));
    }
}


