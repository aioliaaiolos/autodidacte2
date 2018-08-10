package com.autodidacte;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.security.*;
import java.util.Arrays;
//import android.app.

public class MainActivity extends AppCompatActivity {


    boolean mCredentialsValidated = false;
	private static final int RC_HANDLE_CAMERA_PERM = 2;
    static int MY_DATA_CHECK_CODE = 1;

    public void checkTextToSpeechLanguage()
    {
        GameEngine.createTextToSpeech(this);
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
        Intent apprendreAlphabet = new Intent(MainActivity.this, AlphabetActivity.class);
        startActivity(apprendreAlphabet);
    }

    class InitTextToSpeechCallback implements GameEngine.InitTextToSpeechCallback
    {
        @Override
        public void execute()
        {
            MainActivity.this.startAlphabetActivity();
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
            _mustFinish = false;
            finish();
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
        setContentView(R.layout.activity_main);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (rc != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
        } else {
            GameEngine.setInitTextToSpeechCallback(new InitTextToSpeechCallback());
            GameEngine.createTextToSpeech(this);
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);

            boolean flush = false;
            if (flush) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("registred");
                editor.apply();
            }

            String registred = prefs.getString("registred", "false");
            mCredentialsValidated = (registred.equals("true"));
            mCredentialsValidated = true;
            if (!mCredentialsValidated) {
                final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setView(R.layout.activity_question);
                dlgAlert.setMessage("Entrez le code de securite");
                dlgAlert.setTitle("Code");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog dlg = null;
                                try {
                                    dlg = (AlertDialog) dialog;
                                    EditText editUser = (EditText) dlg.findViewById(R.id.username);
                                    EditText editPass = (EditText) dlg.findViewById(R.id.password);

                                    if ((editUser != null) && (editPass != null)) {
                                        MessageDigest md = MessageDigest.getInstance("SHA-256");

                                        byte[] userHash = {25, 2, 9, 44, -68, 32, 107, 120, -104, 111, -61, -37,
                                                -95, -69, -25, -35, 100, -3, 46, -72, -115, -39, 60, -2, 121, -87,
                                                -71, -85, 47, -33, -56, -78};

                                        byte[] passHash = {-46, -18, 71, 77, -124, -119, 42, -80, 110, -63, -121,
                                                -126, -124, -46, -25, 3, 77, -45, -24, 7, 44, 44, -74, -119, 82,
                                                125, 103, 6, -25, 19, 114, 100};

                                        byte[] byteUser = editUser.getText().toString().getBytes();
                                        byte[] hashUser = md.digest(byteUser);

                                        byte[] bytePass = editPass.getText().toString().getBytes();
                                        byte[] hashPass = md.digest(bytePass);

                                        if (Arrays.equals(userHash, hashUser) && Arrays.equals(passHash, hashPass)) {
                                            mCredentialsValidated = true;
                                        }
                                    }

                                } catch (Exception e) {
                                    String message = e.getMessage();
                                    message = message;
                                } finally {
                                    if (!mCredentialsValidated) {
                                        Intent starterIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(starterIntent);
                                        finish();
                                    } else {
                                        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("registred", "true");
                                        editor.commit();
                                        Intent apprendreAlphabet = new Intent(MainActivity.this, AlphabetActivity.class);
                                        startActivity(apprendreAlphabet);
                                    }
                                }
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        }
    }





}
