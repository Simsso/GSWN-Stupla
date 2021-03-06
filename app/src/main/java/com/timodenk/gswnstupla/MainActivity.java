package com.timodenk.gswnstupla;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String ELEMENT_ID_MESSAGE = "com.timodenk.gswnstupla.ELEMENT_ID_MESSAGE",
            PREFS_NAME = "GSWN_Stupla_Preferences",
            SOURCE_CODE_URL = "https://github.com/Simsso/GSWN-Stupla";

    private ListView lvElements;

    private TextView tvMessage, tvSwipeDownMessage;

    private SwipeRefreshLayout elementsSwipeRefreshLayout;
    private ScrollView messagesScrollView;

    private static String[] elements;

    private int lvElementsFirstVisibleItem = -1;
    private boolean lvElementsFirstVisibleItemAvailable = false;

    private LocalStorage localStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.localStorage = new LocalStorage(this);

        this.lvElements = (ListView) findViewById(R.id.lvElements);
        this.tvMessage = (TextView) findViewById(R.id.tvMessage);
        this.tvSwipeDownMessage = (TextView) findViewById(R.id.tvSwipeDownMessage);
        this.messagesScrollView = (ScrollView) findViewById(R.id.messagesScrollView);

        // show logo in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.icon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        this.elementsSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.elementsSwipeRefreshLayout);
        this.elementsSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // set first visible item to zero when refreshing
                        // this makes sure that the list will not be scrolled down after it has been fetched from the server
                        localStorage.saveFirstVisibleItem(0);

                        fetchAndShowElementList();
                    }
                }
        );

        fetchAndShowElementList();

        // listen for click on list element
        this.lvElements.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showStupla(position + 1);
            }
        });

        // check whether a stored element id is available
        int savedElementId = localStorage.loadElementId();
        if (((StuplaApplication)getApplication()).firstStart && savedElementId >= 0) {
            // if it is show the stupla of the id
            showStupla(savedElementId);
        }

        lvElements.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (lvElementsFirstVisibleItemAvailable) {
                    MainActivity.this.lvElementsFirstVisibleItem = firstVisibleItem;
                }
                int topRowVerticalPosition = (lvElements == null || lvElements.getChildCount() == 0) ? 0 : lvElements.getChildAt(0).getTop();
                elementsSwipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        ((StuplaApplication)getApplication()).firstStart = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (lvElementsFirstVisibleItem != -1) {
            localStorage.saveFirstVisibleItem(lvElementsFirstVisibleItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_sorce_code:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL));
                startActivity(browserIntent);
                return true;
            /*case R.id.view_feedback:
                Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                // start stupla activity
                startActivity(intent);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fetchAndShowElementList() {
        //showMessage(R.string.loading_element_list, false);

        // show loading information
        if (!this.elementsSwipeRefreshLayout.isRefreshing()) {
            // instead of just calling elementsSwipeRefreshLayout.setRefreshing(true) call it as follows
            // because of this bug (https://code.google.com/p/android/issues/detail?id=77712)
            this.elementsSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.elementsSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

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
                            lvElements.setAdapter(arrayAdapter);

                            // scroll to the position which has last been visible
                            lvElements.setSelection(localStorage.loadFirstVisibleItem());

                            // show the list view
                            showElementListView();

                            // enable saving the first visible element onScroll
                            lvElementsFirstVisibleItemAvailable = true;
                        }
                    });

                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage(R.string.unknown_server_error, true);
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage(R.string.server_not_available, true);
                        }
                    });
                    e.printStackTrace();
                } catch (final ServerCantProvideServiceException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getServerMessage().equals("")) {
                                showMessage(R.string.unknown_server_error, true);
                            }
                            else {
                                showMessage(e.getServerMessage(), true);
                            }
                        }
                    });
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            elementsSwipeRefreshLayout.setRefreshing(false);
                            elementsSwipeRefreshLayout.setEnabled(true);
                        }
                    });
                }
            }
        };

        // server request in a separate thread to keep the ui responsive
        thread.start();
    }

    private void showMessage(int stringResourceId, boolean showSwipeDownHint) {
        showMessage(getResources().getString(stringResourceId), showSwipeDownHint);
    }

    private void showMessage(String message, boolean showSwipeDownHint) {
        // hide list
        lvElements.setVisibility(View.INVISIBLE);

        tvMessage.setText(message);
        tvSwipeDownMessage.setVisibility(showSwipeDownHint ? View.VISIBLE : View.INVISIBLE);

        // show messages
        messagesScrollView.setVisibility(View.VISIBLE);
    }

    private void showElementListView() {
        // hide messages
        messagesScrollView.setVisibility(View.INVISIBLE);

        // show list
        lvElements.setVisibility(View.VISIBLE);
    }

    private void showStupla(int elementId) {
        // save which element the user uses
        localStorage.saveElementId(elementId);

        Intent intent = new Intent(MainActivity.this, StuplaActivity.class);
        // pass the element id to the stupla activity
        intent.putExtra(ELEMENT_ID_MESSAGE, elementId);
        // start stupla activity
        startActivity(intent);
    }
}
