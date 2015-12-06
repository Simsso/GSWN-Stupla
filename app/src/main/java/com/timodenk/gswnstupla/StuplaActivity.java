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
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.timodenk.view.ObservableWebView;


public class StuplaActivity extends AppCompatActivity {
    private StuplaControl control;

    public ObservableWebView wvStupla;

    private TextView tvMessage, tvSwipeDownMessage;

    public SwipeRefreshLayout swipeRefreshLayout;

    private MenuItem nextWeek = null, previousWeek = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stupla);


        this.tvMessage = (TextView) findViewById(R.id.tvMessage);
        this.tvSwipeDownMessage = (TextView) findViewById(R.id.tvSwipeDownMessage);


        // web view settings
        this.wvStupla = (ObservableWebView) findViewById(R.id.wvStupla);
        wvStupla.getSettings().setLoadWithOverviewMode(true);
        wvStupla.getSettings().setUseWideViewPort(true); // zoom out
        wvStupla.getSettings().setBuiltInZoomControls(true); // allow to zoom in
        wvStupla.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(true); // show loading information
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.equals(StuplaControl.URL_ABOUT_BLANK)) {
                    swipeRefreshLayout.setRefreshing(false); // hide loading information
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                showMessage("Error " + String.valueOf(error.getErrorCode()), true);
                swipeRefreshLayout.setRefreshing(false); // hide loading information
            }
        });

        // activate swipe down to refresh only if the WebView is scrolled to the top
        wvStupla.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t) {
                swipeRefreshLayout.setEnabled(wvStupla.getScrollY() == 0);
            }
        });

        // TODO: disable swipe down to refresh when touching with two fingers

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

        this.nextWeek = menu.findItem(R.id.next_week);
        this.previousWeek = menu.findItem(R.id.previous_week);

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

    public void showMessage(int stringResourceId, boolean showSwipeDownHint) {
        showMessage(getResources().getString(stringResourceId), showSwipeDownHint);
    }

    public void showMessage(String message, boolean showSwipeDownHint) {
        wvStupla.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
        tvSwipeDownMessage.setVisibility(showSwipeDownHint ? View.VISIBLE : View.INVISIBLE);
    }

    public void showWebView() {
        tvMessage.setVisibility(View.INVISIBLE);
        tvSwipeDownMessage.setVisibility(View.INVISIBLE);

        wvStupla.setVisibility(View.VISIBLE);
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


