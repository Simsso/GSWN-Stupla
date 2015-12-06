package com.timodenk.gswnstupla;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    public static final String ELEMENT_ID_MESSAGE = "com.timodenk.gswnstupla.ELEMENT_ID_MESSAGE", PREFS_NAME = "GSWN_Stupla_Preferences";

    private ListView lvElements;

    private TextView tvMessage, tvSwipeDownMessage;

    private SwipeRefreshLayout elementsSwipeRefreshLayout;
    private ScrollView messagesScrollView;

    private static String[] elements;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        int savedElementId = LocalStorage.loadElementId(this);
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
                int topRowVerticalPosition = (lvElements == null || lvElements.getChildCount() == 0) ? 0 : lvElements.getChildAt(0).getTop();
                elementsSwipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        ((StuplaApplication)getApplication()).firstStart = false;
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
                            showElementListView();
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
        tvSwipeDownMessage.setVisibility(showSwipeDownHint ? View.VISIBLE : View.INVISIBLE);;

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
        LocalStorage.saveElementId(this, elementId);

        Intent intent = new Intent(MainActivity.this, StuplaActivity.class);
        // pass the element id to the stupla activity
        intent.putExtra(ELEMENT_ID_MESSAGE, elementId);
        // start stupla activity
        startActivity(intent);
    }
}
