package com.autodidacte;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AlphabetActivity extends Activity {


    Button _trouverLettre = null;
    Button _trouverPremiereLettre = null;
    Button _trouverMot = null;
    Button _sortie = null;
    Button _options = null;
    Button _aide = null;

    class OnVideoReadyCallback implements Utils.IOnVideoReadyCallback
    {
        public void execute(VideoView video)
        {
            _trouverLettre = (Button)findViewById(R.id.trouverLettre);
            _trouverMot = (Button)findViewById(R.id.trouverMot);
            _trouverPremiereLettre = (Button)findViewById(R.id.trouverPremiereLettre);

            Button arr[] = {_trouverLettre, _trouverPremiereLettre, _trouverMot/*, _sortie, _options, _aide*/}; //new Button[6];

            int color = 0xAA888888;
            for(int i = 0; i < arr.length; i++)
            {
                Button b = arr[i];
                b.setBackgroundColor(color);
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);


            int w = video.getWidth();
            int h = video.getHeight();

            int precision = 10000;

            int xLettre = 4300;
            int yLettre = 700;
            int wLettre = 1400;
            int hLettre = 1900;

            int xPremiereLettre = 5000;
            int yPremiereLettre = 7000;
            int wPremiereLettre = 1000;
            int hPremiereLettre = 2100;

            int xMot = 7900;
            int yMot = 2650;
            int wMot = 700;
            int hMot = 1900;

            _trouverLettre.setX(w * xLettre / precision);
            _trouverLettre.setY(h * yLettre / precision);
            _trouverLettre.setLayoutParams(new RelativeLayout.LayoutParams(w * wLettre / precision,h * hLettre / precision));

            _trouverPremiereLettre.setX(w * xPremiereLettre / precision);
            _trouverPremiereLettre.setY(h * yPremiereLettre / precision);
            _trouverPremiereLettre.setLayoutParams(new RelativeLayout.LayoutParams(w * wPremiereLettre / precision,h * hPremiereLettre / precision));

            _trouverMot.setX(w * xMot / precision);
            _trouverMot.setY(h * yMot / precision);
            _trouverMot.setLayoutParams(new RelativeLayout.LayoutParams(w * wMot / precision,h * hMot / precision));

            GameEngine.configureGeneralButtons(AlphabetActivity.this, w, h, R.id.retour, R.id.options, R.id.aide);
        }
    }


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

    OnVideoReadyCallback _onVideoReadyCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_alphabet);

        if(_onVideoReadyCallback == null)
            _onVideoReadyCallback = new OnVideoReadyCallback();

        Utils.setOnVideoReadyCallback(_onVideoReadyCallback);
        //Utils.stopVideo();
        Utils.playVideo(this, R.raw.mainmenu);
        mVisible = true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        GameEngine.setFirstTime();
        if (GameEngine.returnToAlphabetActvity)
            GameEngine.returnToAlphabetActvity = false;

        if (_onVideoReadyCallback == null)
            _onVideoReadyCallback = new OnVideoReadyCallback();
        Utils.setOnVideoReadyCallback(_onVideoReadyCallback);
        //Utils.stopVideo();
        Utils.playVideo(this, R.raw.mainmenu);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        MainActivity.mustFinish();
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
        GameEngine.setGameType(GameEngine.GameType.eTrouverLettre);
        Utils.setOnVideoReadyCallback(null);
        Utils.stopVideo();
        Intent menuLettre = new Intent(AlphabetActivity.this, MenuActivity.class);
        startActivity(menuLettre);
    }

    public void trouverMot(View v)
    {
        GameEngine.setGameType(GameEngine.GameType.eTrouverMot);
        Utils.setOnVideoReadyCallback(null);
        Utils.stopVideo();
        Intent menuMot = new Intent(AlphabetActivity.this, MenuActivity.class);
        startActivity(menuMot);
    }

    public void trouver1ereLettre(View v)
    {
        GameEngine.setGameType(GameEngine.GameType.eTrouverPremiereLettre);
        Utils.setOnVideoReadyCallback(null);
        Utils.stopVideo();
        Intent menuPremiereLettre = new Intent(AlphabetActivity.this, MenuActivity.class);
        startActivity(menuPremiereLettre);
    }


    public void retour(View view)
    {
        MainActivity.mustFinish();
        GameEngine.retour(this, view);
    }

    public void options(View view)
    {
        GameEngine.options(this, view);
    }

}
