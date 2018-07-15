package com.autodidacte;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.security.*;
import java.util.Arrays;

import static java.security.AccessController.getContext;
import static java.security.spec.MGF1ParameterSpec.SHA1;
//import android.app.

public class MainActivity extends AppCompatActivity {

    private Button mApprendreAphabet;
    boolean mCredentialsValidated = false;

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        boolean flush = false;
        if(flush) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("registred");
            editor.apply();
        }


        String registred = prefs.getString("registred", "false");
        mCredentialsValidated = (registred.equals("true"));
        if(!mCredentialsValidated) {
            final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setView(R.layout.dialog_security_code);
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

                                    if(Arrays.equals(userHash, hashUser) && Arrays.equals(passHash, hashPass)){
                                        mCredentialsValidated = true;
                                    }
                                }

                            } catch (Exception e) {
                                String message = e.getMessage();
                                message = message;
                            }
                            finally {
                                if(!mCredentialsValidated) {
                                    Intent starterIntent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(starterIntent);
                                    finish();
                                }
                                else{
                                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("registred", "true");
                                    editor.commit();
                                }
                            }
                        }
                    });
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
       }

        mApprendreAphabet = (Button)findViewById(R.id.activity_main_alphabet_txt);

        mApprendreAphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                if(mCredentialsValidated) {
                    Intent apprendreAlphabet = new Intent(MainActivity.this, ApprendreAlphabetActivity.class);
                    startActivity(apprendreAlphabet);
                }
                else {
                    Intent starterIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(starterIntent);
                    finish();
                }
            }
        });

/*
        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.setMediaController(new MediaController(this));
        video.setVideoURI(Uri.parse("file://sdcard/video/example.avi"));
        video.start();*/




    }
}
