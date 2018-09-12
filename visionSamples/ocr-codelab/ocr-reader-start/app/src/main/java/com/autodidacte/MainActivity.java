package com.autodidacte;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
//import android.app.

public class MainActivity extends AppCompatActivity {


    boolean mCredentialsValidated = false;
	private static final int RC_HANDLE_CAMERA_PERM = 2;
    static int MY_DATA_CHECK_CODE = 1;

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
            //_mustFinish = false;

            ImageView aurevoir = (ImageView)findViewById(R.id.aurevoir);
            aurevoir.setBackgroundResource(R.drawable.aurevoir);
        }
    }

    public static boolean _mustFinish = false;

    public static void mustFinish()
    {
        _mustFinish = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        if(_mustFinish)
            return;


        boolean test = false;
        if (test)
        {
            Intent test2 = new Intent(MainActivity.this, test2Activity.class);
            startActivity(test2);
        }
        else {

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
            }
        }
    }

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
