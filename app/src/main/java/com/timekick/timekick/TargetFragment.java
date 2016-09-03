package com.timekick.timekick;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TargetFragment extends Fragment {

    TextToSpeech tts;
    Timer timer;
    TimerTask timerTask;
    TextView timerTextView;
    TimePicker timePicker;
    ImageButton startStopButton;
    long durationSeconds;
    String durationString;
    Date targetDate;

    ImageButton saveFavoriteButton;
    ImageButton loadFavoriteButton;

    private AlarmManagerBroadcastReceiver alarm;
    final Handler handler = new Handler();

    private MediaPlayer mp;

    public TargetFragment() {
        // Required empty public constructor
    }

    public static String ACTIVITY_TYPE = "Target";

    public static TargetFragment newInstance(int sectionNumber) {
        TargetFragment fragment = new TargetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_target, container, false);
        timePicker = (TimePicker)rootView.findViewById(R.id.timePicker);
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

        startStopButton = (ImageButton)rootView.findViewById(R.id.startStopButton);
        //startStopButton.setZ(1000.0f);

        startStopButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if (!isAlarmActive()) {
                            final Dialog dialog = new Dialog(rootView.getContext(), R.style.CustomDialog);
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
                                    final Dialog intervalDialog = new Dialog(rootView.getContext(), R.style.CustomDialog);
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
        return (alarm != null && getContext() != null && alarm.isReminderRunning(getContext(), AlarmManagerBroadcastReceiver.TARGET_MODE_ID));
    }

    private boolean isAnyActive() {
        return (alarm != null && getContext() != null && alarm.isAnyReminderRunning(getContext()));
    }

    private void updateStartStopButton() {
        if (isAlarmActive()) {
            if (targetDate == null && getActivity() != null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (sharedPref != null && sharedPref.contains(getString(R.string.last_target_date_key))) {
                    targetDate = new Date(sharedPref.getLong(getString(R.string.last_target_date_key), 0));
                    updateTimeLabel();
                }
            }
            startStopButton.setImageResource(R.drawable.btn_off);
            timerTextView.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.INVISIBLE);
        } else {
            startStopButton.setImageResource(R.drawable.btn_on);
            targetDate = null;
            timerTextView.setVisibility(View.INVISIBLE);
            timePicker.setVisibility(View.VISIBLE);
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
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date date = cal.getTime();
        if (date.getTime()<System.currentTimeMillis()) {
            if (getContext() != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Invalid Target")
                        .setMessage("Please select a time that has not passed.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
            }
            return;
        }
        targetDate=date;

        if (getActivity() != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPref != null) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(getString(R.string.last_target_date_key), targetDate.getTime());
                editor.commit();
            }
        }

        startStopButton.setImageResource(R.drawable.btn_off);
        timerTextView.setVisibility(View.VISIBLE);
        timerTextView.setText("Starting...");
        timePicker.setVisibility(View.INVISIBLE);

        StringBuilder msgStr = new StringBuilder();
        Format formatter = new SimpleDateFormat("h:mm a");
        msgStr.append(formatter.format(targetDate));
        msgStr.append(" target.");
        String toSpeak = msgStr.toString();
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

        //Log Facebook event
        Bundle parameters = new Bundle();
        parameters.putString("type", "Target");
        parameters.putString("reminderDuration", durationString);
        AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
        logger.logEvent("start", parameters);

        startRepeatingTimer(toSpeak);
        Log.d("AlarmService", "alarm set");

        updateStartStopButton();
    }

    public void startRepeatingTimer(String toSpeak) {
        alarm = new AlarmManagerBroadcastReceiver();

        Context context = getActivity().getApplicationContext();
        if (alarm != null) {
            alarm.SetTargetReminder(context, durationSeconds, targetDate.getTime());
        } else {
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelRepeatingTimer() {
        Context context = getActivity().getApplicationContext();
        if (alarm != null) {
            alarm.CancelAlarm(context);
        }
        if (mp != null) {
            mp.stop();
            mp = null;
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

        if (alarm == null) {
            alarm = new AlarmManagerBroadcastReceiver();
        }
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

    @Override
    public void onDestroy() {
        //Close the Text to Speech Library
        if(tts != null) {

            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void updateTimeLabel() {
        if (timerTextView != null && timerTextView.getVisibility() == View.VISIBLE && targetDate != null) {
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            long difference = targetDate.getTime() - currentDate.getTime();
            String display = "Time Up";
            if (difference>0) {
                display = convertSecondsToHMmSs(difference / 1000);
            } else {
                handleTimeUp();
            }

            timerTextView.setText(display);
        }
        updateSaveButtons();
        updateStartStopButton();
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }

    public void saveFavoritesButtonPressed() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPref != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.favorite_mode_key), ACTIVITY_TYPE);
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
                String favMode = sharedPref.getString(getString(R.string.favorite_mode_key), null);
                if (favMode != null && favMode.equals(ACTIVITY_TYPE)) {
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

    private void handleTimeUp() {
        if (mp == null) {
            mp = MediaPlayer.create(getContext(),R.raw.alarmclock);
            mp.start();
        }
    }
}
