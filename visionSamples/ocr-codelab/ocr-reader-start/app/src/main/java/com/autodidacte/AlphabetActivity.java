package com.autodidacte;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.autodidacte.Utils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AlphabetActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    // View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
        }
    };
    //private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_alphabet);
        Utils.playVideo(this, R.raw.mainmenu);
        mVisible = true;
    }

    @Override
    protected void onResume() {

        super.onResume();
        Utils.playVideo(this, R.raw.mainmenu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void trouverLettre(View v)
    {
        //OcrCaptureActivity._gameType = OcrCaptureActivity.GameType.eTrouverLettre;
        Intent ocrCaptureActivity = new Intent(AlphabetActivity.this, MenuLettreActivity.class);
        startActivity(ocrCaptureActivity);
    }

    public void trouverMot(View v)
    {
        //OcrCaptureActivity._gameType = OcrCaptureActivity.GameType.eTrouverMot;
        Intent ocrCaptureActivity = new Intent(AlphabetActivity.this, MenuMotActivity.class);
        startActivity(ocrCaptureActivity);
    }

    public void trouver1ereLettre(View v)
    {
        //OcrCaptureActivity._gameType = OcrCaptureActivity.GameType.eTrouver1ereLettre;
        Intent ocrCaptureActivity = new Intent(AlphabetActivity.this, MenuPremiereLettreActivity.class);
        startActivity(ocrCaptureActivity);
    }

    public void exit(View v)
    {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void openOptions(View v)
    {
        Intent ocrCaptureActivity = new Intent(AlphabetActivity.this, MenuOptionsActivity.class);
        startActivity(ocrCaptureActivity);
    }

    public void openHelp(View v)
    {
    }

}
