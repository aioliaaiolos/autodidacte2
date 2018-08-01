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
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MenuLettreActivity extends Activity {
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
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

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

    Button _trouverLettre = null;

    class OnVideoReadyCallback implements Utils.IOnVideoReadyCallback
    {
        public void execute(VideoView video)
        {
            _trouverLettre = (Button)findViewById(R.id.trouver_lettre);
            Button arr[] = {_trouverLettre}; //new Button[6];

            int color = 0xAAFF8888;
            for(int i = 0; i < arr.length; i++)
            {
                Button b = arr[i];
                b.setBackgroundColor(color);
            }

            int w = video.getWidth();
            int h = video.getHeight();

            GameEngine.configureGeneralButtons(MenuLettreActivity.this, w, h, R.id.retour, R.id.options, R.id.aide);

            int precision = 10000;

            int xLettre = 1600;
            int yLettre = 2000;
            int wLettre = 3700;
            int hLettre = 4200;

            _trouverLettre.setX(w * xLettre / precision);
            _trouverLettre.setY(h * yLettre / precision);

            int width = w * wLettre / precision;
            int height = h * hLettre / precision;

            _trouverLettre.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        }
    }

    OnVideoReadyCallback _onVideoReadyCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_menu_lettre);
        //mVisible = true;

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
        else {
            Utils.setOnVideoReadyCallback(_onVideoReadyCallback);
            Utils.stopVideo();
            Utils.playVideo(this, R.raw.lettremenu);
        }
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

    public void lettre(View view)
    {
        GameEngine.setGameType(GameEngine.GameType.eTrouverLettre);
        Intent questionLettre = new Intent(MenuLettreActivity.this, QuestionActivity.class);
        startActivity(questionLettre);

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
