package com.autodidacte;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SubMenuActivity extends Activity implements MediaPlayer.OnVideoSizeChangedListener {


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

    private VideoView _video = null;

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

    Button _bouton = null;

    class Rectangle {
        public int x;
        public int y;
        public int w;
        public int h;

        Rectangle(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private Rectangle computeButtonPositionFromGameType(GameEngine.GameType type) {
        int x = 0, y = 0, w = 0, h = 0;
        if (type == GameEngine.GameType.eTrouverMot) {
            x = 2500;
            y = 1900;
            w = 2000;
            h = 4700;
        } else if (type == GameEngine.GameType.eTrouverLettre) {
            x = 1600;
            y = 2000;
            w = 3700;
            h = 4400;
        } else if (type == GameEngine.GameType.eTrouverPremiereLettre) {
            x = 3000;
            y = 1250;
            w = 2400;
            h = 5600;
        }
        Rectangle rect = new Rectangle(x, y, w, h);
        return rect;
    }

    public void configureButton(VideoView video) {
        //int color = 0xAA888888;
        int color = 0x00000000;
        _bouton = (Button) findViewById(R.id.bouton);
        _bouton.setBackgroundColor(color);
        int wScreen = video.getWidth();
        int hScreen = video.getHeight();

        GameEngine.configureGeneralButtons(SubMenuActivity.this, wScreen, hScreen, R.id.retour, R.id.options, R.id.aide);

        Rectangle rect = computeButtonPositionFromGameType(GameEngine.getGameType());

        int precision = 10000;
        _bouton.setX(wScreen * rect.x / precision);
        _bouton.setY(hScreen * rect.y / precision);
        int w = wScreen * rect.w / precision;
        int h = hScreen * rect.h / precision;
        _bouton.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
    }

    class OnVideoReadyCallback implements Utils.IOnVideoReadyCallback {
        public void execute(VideoView video) {
            _video = video;
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SubMenuActivity.this.runOnUiThread(Timer_Tick);
                }

            }, 1000);
        }
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            configureButton(_video);
        }
    };

    class OnVideoCompletionCallback implements MediaPlayer.OnCompletionListener
    {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(_video != null) {
                configureButton(_video);
            }
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mp = mp;
        /*
        videoWidth = width;
        videoHeight = height;
        Toast.makeText(getApplicationContext(),
                String.valueOf(videoWidth) + "x" + String.valueOf(videoHeight),
                Toast.LENGTH_SHORT).show();

        if (mediaPlayer.isPlaying()){
            surfaceHolder.setFixedSize(videoWidth, videoHeight);
        }*/

    }

    private void playCurrentMenuSound(float volume)
    {
        int soundId = 0;
        String resName = "";
        if(GameEngine.getGameType() ==  GameEngine.GameType.eTrouverLettre) {
            soundId = R.raw.soundmajmin;
            resName = "soundmajmin";
        }
        else if(GameEngine.getGameType() ==  GameEngine.GameType.eTrouverPremiereLettre) {
            soundId = R.raw.soundpremierelettre;
            resName = "soundPremiereLettre";
        }
        else if(GameEngine.getGameType() ==  GameEngine.GameType.eTrouverMot) {
            soundId = R.raw.soundword;
            resName = "soundWord";
        }
        Utils.playSound(getApplicationContext(), soundId, false,1.0f * volume, resName);
        Utils.playSound(getApplicationContext(), R.raw.musicsousmenu, false,0.5f * volume, "musicsousmenu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_submenu);

        //Utils.setOnVideoReadyCallback(new OnVideoReadyCallback());
        //Utils.setOnVideoCompletionCallback(new OnVideoCompletionCallback());
        //Utils.playVideo(this, GameEngine.getVideoFromGameType(GameEngine.getGameType(), this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GameEngine.returnToAlphabetActvity)
            finish();
        else {
            playCurrentMenuSound(0.5f);
            Utils.setOnVideoReadyCallback(new OnVideoReadyCallback());
            //Utils.setOnVideoCompletionCallback(new OnVideoCompletionCallback());
            //Utils.stopVideo();
            Utils.playVideo(this, GameEngine.getVideoFromGameType(GameEngine.getGameType(), this));
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

    public void launch(View view) {
        Utils.stopSounds();
        Utils.playSound(SubMenuActivity.this, R.raw.soundclicksubmenu, false, 100, "soundclicksubmenu");
        Intent questionActivity = new Intent(this, QuestionActivity.class);
        startActivity(questionActivity);
    }
}