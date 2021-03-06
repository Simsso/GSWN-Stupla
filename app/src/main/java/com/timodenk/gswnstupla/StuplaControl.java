package com.timodenk.gswnstupla;

import org.json.JSONException;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


class StuplaControl {
    static final String URL_ABOUT_BLANK = "about:blank";

    private GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMAN);

    private final StuplaActivity ui;

    private final int chosenElementId;
    private int chosenWeek;

    private int[] availableWeeks = null;

    private String chosenElementName;


    StuplaControl(StuplaActivity stuplaActivity, int chosenElementId) {
        this.ui = stuplaActivity;
        this.chosenElementId = chosenElementId;

        // read passed element id, defines chosen week, updates web view and action bar text
        initialize();
    }

    private void initialize() {
        // set week to current week
        this.chosenWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        // show next week on sunday
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            if (calendar.getFirstDayOfWeek() == Calendar.MONDAY) {
                this.chosenWeek = incrementWeek(this.chosenWeek);
            }

            // show information that the user sees the next week
            this.ui.showToast(R.string.showing_next_week);
        }

        // updates the url of the web view
        updateWebView();

        updateTaskBarElementName();

        downloadAvailableWeeks();
    }

    void downloadAvailableWeeks() {
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
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.unknown_server_error, true);
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.server_not_available, true);
                        }
                    });
                    e.printStackTrace();
                } catch (final ServerCantProvideServiceException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getServerMessage().equals("")) {
                                ui.showMessage(R.string.unknown_server_error, true);
                            } else {
                                ui.showMessage(e.getServerMessage(), true);
                            }
                        }
                    });
                    e.printStackTrace();
                } finally {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.swipeRefreshLayout.setEnabled(true);
                        }
                    });
                }
            }
        };

        thread.start();
    }

    void updateTaskBarElementName() {
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
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.unknown_server_error, true);
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.server_not_available, true);
                        }
                    });
                    e.printStackTrace();
                } catch (final ServerCantProvideServiceException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getServerMessage().equals("")) {
                                ui.showMessage(R.string.unknown_server_error, true);
                            }
                            else {
                                ui.showMessage(e.getServerMessage(), true);
                            }
                        }
                    });
                    e.printStackTrace();
                } finally {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.swipeRefreshLayout.setEnabled(true);
                        }
                    });
                }
            }
        };

        thread.start();
    }


    void updateWebView() {
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
                            ui.showWebView();
                        }
                    });
                } catch (JSONException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.unknown_server_error, true);

                            ui.swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.showMessage(R.string.server_not_available, true);

                            ui.swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    e.printStackTrace();
                } catch (final ServerCantProvideServiceException e) {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getServerMessage().equals("")) {
                                ui.showMessage(R.string.unknown_server_error, true);
                            }
                            else {
                                ui.showMessage(e.getServerMessage(), true);
                            }

                            ui.swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    e.printStackTrace();
                } finally {
                    ui.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ui.swipeRefreshLayout.setEnabled(true);
                        }
                    });
                }
            }
        };

        thread.start();
    }

    boolean incrementChosenWeek() {
        int tmp = incrementWeek(this.chosenWeek);

        if (incrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    boolean decrementChosenWeek() {
        int tmp = decrementWeek(this.chosenWeek);

        if (decrementWeekAvailable(tmp)) {
            this.chosenWeek = tmp;
            return true;
        }
        return false;
    }

    boolean incrementWeekAvailable() {
        return incrementWeekAvailable(incrementWeek(this.chosenWeek));
    }

    private boolean incrementWeekAvailable(int newWeek) {
        return (!(this.availableWeeks == null || this.availableWeeks.length == 0) && (weekAvailable(newWeek) || this.availableWeeks[0] > newWeek && newWeek > 1 && !weekAvailable(decrementWeek(newWeek))));
    }

    boolean decrementWeekAvailable() {
        return decrementWeekAvailable(decrementWeek(this.chosenWeek));
    }

    private boolean decrementWeekAvailable(int newWeek) {
        return (!(this.availableWeeks == null || this.availableWeeks.length == 0) && (weekAvailable(newWeek) || this.availableWeeks[this.availableWeeks.length - 1] < newWeek && newWeek < getNumberOfWeeksInYearBefore() && !weekAvailable(incrementWeek(newWeek))));
    }


    private boolean weekAvailable(int week) {
        if (this.availableWeeks == null) {
            return true;
        }

        for (int availableWeek : this.availableWeeks) {
            if (availableWeek == week) {
                return true;
            }
        }
        return false;
    }

    private static int incrementWeek(int week) {
        week++;

        while (week > getNumberOfWeeksInYearBefore()) {
            week -= getNumberOfWeeksInYearBefore();
        }

        return week;
    }

    private static int decrementWeek(int week) {
        week--;

        while (week <= 0) {
            week += getNumberOfWeeksInYearBefore();
        }

        return week;
    }

    private static int getNumberOfWeeksInYearBefore() {
        return getNumberOfWeeksOfYear(Calendar.getInstance().get(Calendar.YEAR) - 1);
    }

    private static int getNumberOfWeeksOfCurrentYear() {
        return getNumberOfWeeksOfYear(Calendar.getInstance().get(Calendar.YEAR));
    }

    private static int getNumberOfWeeksOfYear(int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        c.set(Calendar.DAY_OF_MONTH, 31);

        int ordinalDay = c.get(Calendar.DAY_OF_YEAR);
        int weekDay = c.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        return (ordinalDay - weekDay + 10) / 7; // number of weeks
    }
}
