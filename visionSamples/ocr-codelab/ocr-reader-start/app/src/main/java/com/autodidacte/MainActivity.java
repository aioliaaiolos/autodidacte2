package com.autodidacte;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;
//import android.app.

public class MainActivity extends Activity {


    boolean mCredentialsValidated = false;
	private static final int RC_HANDLE_CAMERA_PERM = 2;
    static int MY_DATA_CHECK_CODE = 1;
    boolean _firstTime = true;

    public void checkTextToSpeechLanguage()
    {
        //GameEngine.createTextToSpeech(this);
        initTextToSpeech(this, new InitTextToSpeechCallback());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GameEngine.INIT_MISSING_LANGUAGE)
        {
            startAlphabetActivity();
        }
    }

    void startAlphabetActivity()
    {
        Intent apprendreAlphabet = new Intent(MainActivity.this, AccueilActivity.class);
        startActivity(apprendreAlphabet);
    }

    static class InitTextToSpeechCallback implements GameEngine.InitTextToSpeechCallback
    {
        @Override
        public void execute(Activity activity)
        {
            MainActivity main = (MainActivity)activity;
            main.startAlphabetActivity();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(grantResults[0] != -1) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            checkTextToSpeechLanguage();
        }
        else
            finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(_mustFinish) {
            ImageView aurevoir = (ImageView)findViewById(R.id.image);
            aurevoir.setAdjustViewBounds(true);
            aurevoir.setBackgroundResource(R.drawable.aurevoir);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int precision = 1000;
            int h = size.y;
            int w = (int)((float)h * (float)(720.0f/702.0f));
            aurevoir.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
            int leftMargin = (size.x - w) / 2;
            aurevoir.setTranslationX(leftMargin);

            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(Timer_Tick_Exit);
                }

            }, 30000);
        }
    }

    private Runnable Timer_Tick_Exit = new Runnable() {
        public void run() {
            System.exit(0);
        }
    };

    public static boolean _mustFinish = false;

    public static void mustFinish()
    {
        _mustFinish = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if(_firstTime) {
            setContentView(R.layout.activity_main);
            ImageView loading = (ImageView) findViewById(R.id.image);
            loading.setBackgroundResource(R.drawable.loading);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int precision = 1000;
            int h = size.y;
            int w = (int) ((float) h * (float) (720.0f / 702.0f));
            loading.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
            int leftMargin = (size.x - w) / 2;
            loading.setTranslationX(leftMargin);
            _firstTime = false;
        }

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(Timer_Tick);
            }

        }, 10000);

        /*
        if(_mustFinish)
            return;

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
        } else {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);

            boolean flush = false;
            if (flush) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("registred");
                editor.apply();
            }
            initTextToSpeech(this, new InitTextToSpeechCallback());
        }*/
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            if(_mustFinish)
                return;

            int rc = ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.CAMERA);
            if (rc != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.CAMERA};
                ActivityCompat.requestPermissions(MainActivity.this, permissions, RC_HANDLE_CAMERA_PERM);
            } else {
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);

                boolean flush = false;
                if (flush) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("registred");
                    editor.apply();
                }
                initTextToSpeech(MainActivity.this, new InitTextToSpeechCallback());
            }
        }
    };


    public static void initTextToSpeech(Activity activity, GameEngine.InitTextToSpeechCallback initCallback)
    {
        GameEngine._currentActivity = activity;
        GameEngine._initCallback = initCallback;
        GameEngine.tts = new TextToSpeech(activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "Text to speech engine started successfully.");
                    int lang = GameEngine.tts.setLanguage(GameEngine.currentLanguage());
                    //lang = TextToSpeech.LANG_NOT_SUPPORTED;
                    if (lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Intent installIntent = new Intent();
                        installIntent.setAction(
                                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        GameEngine._currentActivity.startActivityForResult(installIntent, GameEngine.INIT_MISSING_LANGUAGE);
                    } else {
                        GameEngine._init = true;
                        if (GameEngine._initCallback != null)
                            GameEngine._initCallback.execute(GameEngine._currentActivity);
                    }
                } else {
                    Log.d("TTS", "Error starting the text to speech engine.");
                }
            }
        });
    }





}
