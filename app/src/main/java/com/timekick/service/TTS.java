package com.timekick.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.timekick.timekick.R;
import com.timekick.timekick.SettingsActivity;

import static java.util.concurrent.TimeUnit.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pbeyer on 3/5/16.
 */
public class TTS extends IntentService implements TextToSpeech.OnInitListener,TextToSpeech.OnUtteranceCompletedListener {

    public TTS() {
        super("TTS");
    }

    private TextToSpeech mTts;
    private String mode;
    private long targetTime;

    @Override
    public void onCreate() {
        mTts = new TextToSpeech(this, this);
        targetTime = 0;

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        // This is a good place to set spokenText
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
            speakOut();
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleDefaultSettings(intent);
        speakOut();
    }

    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleDefaultSettings(intent);
        speakOut();
    }

    private void handleDefaultSettings(Intent intent) {
        if (intent.hasExtra("TARGET")) {
            targetTime = intent.getLongExtra("TARGET",0);
        } else {
            targetTime = 0;
        }

        if (intent.hasExtra("MODE")) {
            mode = intent.getStringExtra("MODE");
        } else {
            mode = null;
        }
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        stopSelf();
    }

    private void speakOut() {
        if (targetTime > 0) {
            speakTimeRemaining(targetTime);
        } else {
            speakCurrentTime();
        }
    }

    private void speakCurrentTime() {
        StringBuilder msgStr = new StringBuilder();
        Format formatter = new SimpleDateFormat("h:mm a");
        msgStr.append(formatter.format(new Date()));
        if (msgStr.toString().split(":")[1].startsWith("0") && !msgStr.toString().split(":")[1].startsWith("00")) {
            msgStr.replace(msgStr.indexOf(":")+1,msgStr.indexOf(":")+2,"O");
        }
        mTts.speak(msgStr.toString(), TextToSpeech.QUEUE_FLUSH, null);

        Boolean show_toast = true;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPref != null) {
            show_toast = sharedPref.getBoolean(SettingsActivity.KEY_SHOW_TOAST, Boolean.parseBoolean(""));
        }
        if (show_toast) {
            Toast toast = Toast.makeText(getApplicationContext(), "TimeKick: " + msgStr.toString(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }
    }

    private void speakTimeRemaining(long targetTime) {
        Date now = new Date();
        Date targetDate = new Date(targetTime);
        long mills = targetDate.getTime() - now.getTime();

        String speech = convertSecondsToHMmSs(mills, mode);
        if (speech != null && speech.length()>0){
            mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            mTts.stop();
        }

        if (!mode.equals("FINAL")) {
            Boolean show_toast = true;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPref != null) {
                show_toast = sharedPref.getBoolean(SettingsActivity.KEY_SHOW_TOAST, Boolean.parseBoolean(""));
            }
            if (show_toast && speech != null && speech.length()>0) {
                Toast toast = Toast.makeText(getApplicationContext(), "TimeKick: " + speech, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        }
    }

    public static String convertSecondsToHMmSs(long millis, String mode) {
        millis = millis + 1000;

        if (mode.equals("FINAL")) {
            long s = MILLISECONDS.toSeconds(millis);
            if (s>=1) {
                return String.format("%d",MILLISECONDS.toSeconds(millis));
            } else {
                return null;
            }
        }
        StringBuilder formattedString = new StringBuilder();

        long h = MILLISECONDS.toHours(millis) % 24;
        if (h > 0) {
            String label = (h > 1)?"hours":"hour";
            formattedString.append(String.format("%d %s ",h,label));
        }
        long m = MILLISECONDS.toMinutes(millis) % 60;
        if (m > 0) {
            String label = (m > 1)?"minutes":"minute";
            formattedString.append(String.format("%d %s ",m,label));
        }
        long s = MILLISECONDS.toSeconds(millis) % 60;
        if (s > 0) {
            String label = (s > 1)?"seconds":"second";
            formattedString.append(String.format("%d %s ",s,label));
        }

        if (formattedString.length() > 0) {
            formattedString.append("left");
        }

        return formattedString.toString();
    }
}
