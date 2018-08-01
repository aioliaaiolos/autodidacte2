package com.autodidacte;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MenuMotActivity extends Activity {


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
    //private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            /*
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    */
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
            hide();
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

    Button _trouverMot = null;

    class OnVideoReadyCallback implements Utils.IOnVideoReadyCallback
    {
        public void execute(VideoView video)
        {
            _trouverMot = (Button)findViewById(R.id.trouver_mot);

            Button arr[] = {_trouverMot};

            int color = 0xAA888888;
            for(int i = 0; i < arr.length; i++)
            {
                Button b = arr[i];
                b.setBackgroundColor(color);
            }

            int w = video.getWidth();
            int h = video.getHeight();

            GameEngine.configureGeneralButtons(MenuMotActivity.this, w, h, R.id.retour, R.id.options, R.id.aide);

            int precision = 10000;

            int xLettre = 2500;
            int yLettre = 1000;
            int wLettre = 1500;
            int hLettre = 1000;


            _trouverMot.setX(w * xLettre / precision);
            _trouverMot.setY(h * yLettre / precision);
            _trouverMot.setWidth(w * wLettre / precision);
            _trouverMot.setHeight(h * hLettre / precision);
        }
    }

    OnVideoReadyCallback _onVideoReadyCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_menu_mot);

        if(_onVideoReadyCallback == null)
            _onVideoReadyCallback = new OnVideoReadyCallback();

        Utils.setOnVideoReadyCallback(_onVideoReadyCallback);
        Utils.playVideo(this, R.raw.lettremenu);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(GameEngine.returnToAlphabetActvity)
            finish();
        /*
        else if (Utils.currentVideoId() != R.raw.motmenu)
            Utils.playVideo(this, R.raw.motmenu);*/
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.setOnVideoReadyCallback(null);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);*/
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void launchMot(View view)
    {

        GameEngine.setGameType(GameEngine.GameType.eTrouverMot);
        Intent questionActivity = new Intent(MenuMotActivity.this, QuestionActivity.class);
        //startActivity(questionActivity);
        startActivityForResult(questionActivity, GameEngine.QUESTION_ACTIVITY);
    }


    public void retour(View view)
    {
        GameEngine.retour(this, view);

    }

    public void options(View view)
    {
        GameEngine.options(this, view);
    }

    public void aide(View view)
    {
        GameEngine.aide(this, view);
    }
}
