package com.timekick.timekick;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class TimeFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public TimeFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TimeFragment newInstance(int sectionNumber) {
        TimeFragment fragment = new TimeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    TextToSpeech tts;
    Timer timer;
    TimerTask timerTask;
    TextView timerTextView;
    ImageButton startStopButton;
    ImageButton saveFavoriteButton;
    ImageButton loadFavoriteButton;

    long durationSeconds;
    String durationString;

    private AlarmManagerBroadcastReceiver alarm;
    final Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_time, container, false);
        timerTextView = (TextView)rootView.findViewById(R.id.timerTextView);

        alarm = new AlarmManagerBroadcastReceiver();

        tts=new TextToSpeech(rootView.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        startStopButton = (ImageButton)rootView.findViewById(R.id.startStopButton);
        updateStartStopButton();

        saveFavoriteButton = (ImageButton)rootView.findViewById(R.id.pinButton);
        saveFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFavoritesButtonPressed();
            }
        });

        loadFavoriteButton = (ImageButton)rootView.findViewById(R.id.starButton);
        loadFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFavoriteButtonPressed();
            }
        });

        startStopButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if (!isAlarmActive()) {
                            final Dialog dialog = new Dialog(rootView.getContext(),R.style.CustomDialog);
                            dialog.setContentView(R.layout.custom_dialog);
                            dialog.setTitle("Tell Me Every...");
                            Button dialogButtonCancel = (Button) dialog.findViewById(R.id.customDialogCancel);
                            Button dialogButton1Minute = (Button) dialog.findViewById(R.id.customDialog1Minute);
                            Button dialogButton3Minute = (Button) dialog.findViewById(R.id.customDialog3Minute);
                            Button dialogButton5Minute = (Button) dialog.findViewById(R.id.customDialog5Minute);
                            Button dialogButton10Minute = (Button) dialog.findViewById(R.id.customDialog10Minute);
                            Button dialogButtonCustom = (Button) dialog.findViewById(R.id.customDialogCustom);

                            dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialogButton1Minute.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    durationSeconds = 60;
                                    durationString = "1 Minute";
                                    start();
                                    dialog.dismiss();
                                }
                            });
                            dialogButton5Minute.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    durationSeconds = 300;
                                    durationString = "5 Minutes";
                                    start();
                                    dialog.dismiss();
                                }
                            });
                            dialogButton3Minute.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    durationSeconds = 180;
                                    durationString = "3 Minutes";
                                    start();
                                    dialog.dismiss();
                                }
                            });
                            dialogButton10Minute.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    durationSeconds = 600;
                                    durationString = "10 Minutes";
                                    start();
                                    dialog.dismiss();
                                }
                            });
                            dialogButtonCustom.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    final Dialog intervalDialog = new Dialog(rootView.getContext(),R.style.CustomDialog);
                                    intervalDialog.setContentView(R.layout.custom_time_dialog);
                                    final TimePicker t = (TimePicker) intervalDialog.findViewById(R.id.customTimePicker);
                                    t.setIs24HourView(true);
                                    t.setCurrentHour(0);
                                    t.setCurrentMinute(1);

                                    Button cancel = (Button) intervalDialog.findViewById(R.id.customDialogCancel);
                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            intervalDialog.dismiss();
                                        }
                                    });

                                    Button set = (Button) intervalDialog.findViewById(R.id.customDialogSet);
                                    set.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            durationString = "";
                                            Integer hours = t.getCurrentHour();
                                            Integer minutes = t.getCurrentMinute();
                                            int h = hours * 60 * 60;
                                            if (hours > 0) {
                                                if (hours > 1) {
                                                    durationString = hours.toString() + " hours";
                                                } else {
                                                    durationString = "1 hour";
                                                }
                                                if (minutes > 0) {
                                                    durationString += " and ";
                                                }
                                            }
                                            int m = minutes * 60;
                                            if (minutes > 0) {
                                                if (minutes > 1) {
                                                    durationString += minutes.toString() + "minutes";
                                                } else {
                                                    durationString += "1 minute";
                                                }
                                            }

                                            durationSeconds = (h + m);
                                            start();

                                            intervalDialog.dismiss();
                                        }
                                    });
                                    intervalDialog.setTitle("Custom Interval");
                                    intervalDialog.show();
                                }
                            });

                            dialog.show();
                        } else {
                            stop();
                        }
                    }
                }
        );

        return rootView;
    }

    private boolean isAlarmActive() {
        return (alarm != null && getContext() != null && alarm.isReminderRunning(getContext(), AlarmManagerBroadcastReceiver.TIME_MODE_ID));
    }

    private boolean isAnyActive() {
        return (alarm != null && getContext() != null && alarm.isAnyReminderRunning(getContext()));
    }

    private void updateStartStopButton() {
        if (isAlarmActive()) {
            startStopButton.setImageResource(R.drawable.btn_off);
        } else {
            startStopButton.setImageResource(R.drawable.btn_on);
        }
    }

    private void start() {
        if (isAnyActive()) {
            if (getContext() != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Already Running")
                        .setMessage("You can only run a single mode at a time.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
            }
            return;
        }
        String toSpeak = "Now reminding you every ";
        if (durationSeconds>60) {
            toSpeak += durationString;
        } else {
            toSpeak = "Now reminding you every minute.";
        }

        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

        //Log Facebook event
        Bundle parameters = new Bundle();
        parameters.putString("type", "Time");
        parameters.putString("reminderDuration", durationString);
        AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
        logger.logEvent("start",parameters);

        startRepeatingTimer(toSpeak);
        Log.d("AlarmService", "alarm set");

        updateStartStopButton();
    }

    public void startRepeatingTimer(String toSpeak) {
        alarm = new AlarmManagerBroadcastReceiver();

        Context context = getActivity().getApplicationContext();
        if (alarm != null) {
            alarm.SetTimeReminder(context, durationSeconds);
        } else {
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelRepeatingTimer() {
        Context context = getActivity().getApplicationContext();
        if (alarm != null) {
            alarm.CancelAlarm(context);
        } else {
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void stop() {
        cancelRepeatingTimer();
        updateStartStopButton();
    }

    @Override
    public void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
        updateTimeLabel();
        AppEventsLogger.activateApp(getContext());

        if (alarm == null) {
            alarm = new AlarmManagerBroadcastReceiver();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(getContext());
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                boolean post = handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        updateTimeLabel();
                    }
                });
            }
        };
    }

    private void updateTimeLabel() {
        Boolean show_seconds = true;
        if (getActivity() != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref != null) {
                show_seconds = sharedPref.getBoolean(SettingsActivity.KEY_VIEW_SECONDS, Boolean.parseBoolean(""));
            }
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma");
        timeFormat.setTimeZone(TimeZone.getDefault());
        if (show_seconds) {
           timeFormat = new SimpleDateFormat("h:mm:ssa");
        }
        final String tm = timeFormat.format(calendar.getTime());
        SpannableString span = new SpannableString(tm);
            span.setSpan(new RelativeSizeSpan(0.4f), span.length() - 2, span.length(), 0);
        timerTextView.setText(span);

        updateStartStopButton();
        updateSaveButtons();
    }

    @Override
    public void onDestroy() {
        //Close the Text to Speech Library
        if(tts != null) {

            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public void saveFavoritesButtonPressed() {
        if (durationString == null || durationSeconds == 0) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Missing Reminder Interval")
                    .setMessage("You must start a timer and select a reminder interval before saving a favorite.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPref != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.favorite_mode_key), MainActivity.ACTIVITY_TYPE);
            editor.putString(getString(R.string.favorite_duration_string_key), durationString);
            editor.putLong(getString(R.string.favorite_duration_key), durationSeconds);
            editor.commit();
        }
        updateSaveButtons();
    }

    private void updateSaveFavoritesButton() {
        if (getActivity() != null) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPref != null) {
            Boolean isFavorite = false;
            String favMode = sharedPref.getString(getString(R.string.favorite_mode_key),null);
            if (favMode != null && favMode.equals(MainActivity.ACTIVITY_TYPE)) {
                isFavorite = true;
            }

            if (isFavorite) {
                saveFavoriteButton.setImageResource(R.drawable.pin_fav);
            } else {
                saveFavoriteButton.setImageResource(R.drawable.pin);
            }
        }
        }
    }

    private void updateLoadFavoritesButton() {
        if (getActivity() != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref != null) {
                if (sharedPref.getString(getString(R.string.favorite_mode_key), null) != null) {
                    loadFavoriteButton.setVisibility(View.VISIBLE);
                    return;
                }
            }
            loadFavoriteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void loadFavoriteButtonPressed() {
        ((MainActivity)getActivity()).loadFavorite();
    }

    public void loadFavorite() {
        if (getActivity() != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref != null) {
                durationSeconds = sharedPref.getLong(getString(R.string.favorite_duration_key),0);
                durationString = sharedPref.getString(getString(R.string.favorite_duration_string_key),"");
            }
        }

        start();
    }

    private void updateSaveButtons() {
        updateSaveFavoritesButton();
        updateLoadFavoritesButton();
    }
}
