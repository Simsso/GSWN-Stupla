package com.timodenk.gswnstupla;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Denk on 21/11/15.
 */
public class StuplaControl {
    public static final String URL_ABOUT_BLANK = "about:blank";

    StuplaActivity ui;

    private int chosenElementId, chosenWeek;

    private int[] availableWeeks = null;

    private String chosenElementName;


    public StuplaControl(StuplaActivity stuplaActivity, int chosenElementId) {
        this.ui = stuplaActivity;
        this.chosenElementId = chosenElementId;

        // read passed element id, defines chosen week, updates webview and action bar text
        initialize();
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
            this.ui.showToast(R.string.showing_next_week);
        }

        // updates the url of the web view
        updateWebView();

        updateTaskBarElementName();

        downloadAvailableWeeks();
    }

    public void downloadAvailableWeeks() {
        // download the available weeks
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    availableWeeks = Server.getAvailableWeeks();
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.updateChangeWeekButtons();
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

    public void updateTaskBarElementName() {
        // download the name of the chosen element
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    chosenElementName = Server.getElementName(chosenElementId);
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.setTitle(chosenElementName);
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


    public void updateWebView() {
        // clear the web view
        this.ui.wvStupla.loadUrl(URL_ABOUT_BLANK);

        // request the url in a separate thread to keep the ui responsive
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final String url = Server.getElementUrl(chosenElementId, chosenWeek);

                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // update web view url
                            ui.wvStupla.loadUrl(url);
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

    public boolean incrementChosenWeek() {
        int tmp = incrementWeek(this.chosenWeek);

        if (incrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    public boolean decrementChosenWeek() {
        int tmp = decrementWeek(this.chosenWeek);

        if (decrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    public boolean incrementWeekAvailable() {
        return incrementWeekAvailable(incrementWeek(this.chosenWeek));
    }

    public boolean incrementWeekAvailable(int newWeek) {
        return (!(this.availableWeeks == null) && (weekAvailable(newWeek) || this.availableWeeks[0] > newWeek));
    }

    public boolean decrementWeekAvailable() {
        return decrementWeekAvailable(decrementWeek(this.chosenWeek));
    }

    public boolean decrementWeekAvailable(int newWeek) {
        return (!(this.availableWeeks == null) && (weekAvailable(newWeek) || this.availableWeeks[this.availableWeeks.length - 1] < newWeek));
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

    public static int incrementWeek(int week) {
        week++;

        while (week > 52) {
            week -= 52;
        }

        return week;
    }

    public static int decrementWeek(int week) {
        week--;

        while (week <= 0) {
            week += 52;
        }

        return week;
    }
}
