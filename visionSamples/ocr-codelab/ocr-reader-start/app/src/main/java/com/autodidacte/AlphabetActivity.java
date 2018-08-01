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
            _sortie = (Button)findViewById(R.id.retour);
            _options = (Button)findViewById(R.id.options);
            _aide = (Button)findViewById(R.id.aide);

            Button arr[] = {_trouverLettre, _trouverPremiereLettre, _trouverMot, _sortie, _options, _aide}; //new Button[6];

            int color = 0xAA888888;
            for(int i = 0; i < arr.length; i++)
            {
                Button b = arr[i];
                b.setBackgroundColor(color);
            }


            int w = video.getWidth();
            int h = video.getHeight();

            int precision = 10000;

            int xLettre = 4000;
            int yLettre = 800;
            int wLettre = 500;
            int hLettre = 400;

            int xPremiereLettre = 4700;
            int yPremiereLettre = 7100;
            int wPremiereLettre = 500;
            int hPremiereLettre = 550;

            int xMot = 7350;
            int yMot = 2650;
            int wMot = 400;
            int hMot = 550;

            int xSortie = 1700;
            int ySortie = 200;
            int wSortie = 700;
            int hSortie = 250;

            int xOptions = 8200;
            int yOptions = 200;
            int wOptions = 600;
            int hOptions = 250;

            int xAide = 8200;
            int yAide = 1200;
            int wAide = 600;
            int hAide = 250;

            _trouverLettre.setX(w * xLettre / precision);
            _trouverLettre.setY(h * yLettre / precision);
            _trouverLettre.setWidth(w * wLettre / precision);
            _trouverLettre.setHeight(h * hLettre / precision);

            _trouverPremiereLettre.setX(w * xPremiereLettre / precision);
            _trouverPremiereLettre.setY(h * yPremiereLettre / precision);
            _trouverPremiereLettre.setWidth(w * wPremiereLettre / precision);
            _trouverPremiereLettre.setHeight(h * hPremiereLettre / precision);

            _trouverMot.setX(w * xMot / precision);
            _trouverMot.setY(h * yMot / precision);
            _trouverMot.setWidth(w * wMot / precision);
            _trouverMot.setHeight(h * yMot / precision);

            _sortie.setX(w * xSortie / precision);
            _sortie.setY(h * ySortie / precision);
            _sortie.setWidth(w * wSortie / precision);
            _sortie.setHeight(h * hSortie / precision);

            _aide.setX(w * xAide / precision);
            _aide.setY(h * yAide / precision);
            _aide.setWidth(w * wAide / precision);
            _aide.setHeight(h * hAide / precision);

            _options.setX(w * xOptions / precision);
            _options.setY(h * yOptions / precision);
            _options.setWidth(w * wOptions / precision);
            _options.setHeight(h * hOptions / precision);
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
        Utils.stopVideo();
        Utils.playVideo(this, R.raw.mainmenu);
        mVisible = true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(GameEngine.returnToAlphabetActvity)
            GameEngine.returnToAlphabetActvity = false;

        if(_onVideoReadyCallback == null)
            _onVideoReadyCallback = new OnVideoReadyCallback();
        Utils.setOnVideoReadyCallback(_onVideoReadyCallback);
        Utils.stopVideo();
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

    public void aide(View view)
    {
        GameEngine.aide(this, view);
    }

}
