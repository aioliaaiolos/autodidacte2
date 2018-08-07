package com.autodidacte;

import android.content.Intent;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Hashtable;
import java.util.Locale;

public class SuccessActivity extends AppCompatActivity {

    private TextToSpeech tts;
    static Hashtable<Character, Integer> charToId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(charToId == null)
        {
            initMapping();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        ImageButton resultButton = (ImageButton)findViewById(R.id.newbutton);
        Intent currentIntent = getIntent();
        String result = currentIntent.getStringExtra("result");
        String currentItem = currentIntent.getStringExtra("currentItem");
       // if(result.equals("success")) {

        // test


        // fin test

            if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
                if(currentItem.length() > 0) {
                    Character c = currentItem.charAt(0);
                    Object o = charToId.get(c);
                    if(o != null) {
                        int res = (int)o;
                        resultButton.setBackgroundResource(res);
                    }
                }
            }
            else
                resultButton.setBackgroundResource(R.drawable.valider);

        //} else
        //    resultButton.setBackgroundResource(R.drawable.error);

        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("TTS", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.FRANCE);
                            Intent currentIntent = getIntent();
                            String sentence = currentIntent.getStringExtra("sentence");
                            tts.setSpeechRate(0.8f);
                            tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {

                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    setResult(1, getIntent());
                                    finish();
                                }

                                @Override
                                public void onError(String utteranceId) {

                                }
                            });
                        } else {
                            Log.d("TTS", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
        tts.setLanguage(Locale.FRANCE);

        Utils.Sleep(1000);
    }
/*
    public void clickNew(View v)
    {
        //Toast.makeText(this, "Show some text on the screen.", Toast.LENGTH_LONG).show();
        setResult(1, getIntent());
        finish();
       // Intent capture = new Intent(OcrCaptureActivity.this, SuccessActivity.class)
    }*/

    static void initMapping()
    {
        charToId = new Hashtable<Character, Integer>();
        charToId.put('a', R.drawable.a);
        charToId.put('b', R.drawable.b);
        charToId.put('c', R.drawable.c);
        charToId.put('d', R.drawable.d);
        charToId.put('e', R.drawable.e);
        charToId.put('f', R.drawable.f);
        charToId.put('g', R.drawable.g);
        charToId.put('h', R.drawable.h);
        charToId.put('i', R.drawable.i);
        charToId.put('j', R.drawable.j);
        charToId.put('k', R.drawable.k);
        charToId.put('l', R.drawable.l);
        charToId.put('m', R.drawable.m);
        charToId.put('n', R.drawable.n);
        charToId.put('o', R.drawable.o);
        charToId.put('p', R.drawable.p);
        charToId.put('q', R.drawable.q);
        charToId.put('r', R.drawable.r);
        charToId.put('s', R.drawable.s);
        charToId.put('t', R.drawable.t);
        charToId.put('u', R.drawable.u);
        charToId.put('v', R.drawable.v);
        charToId.put('w', R.drawable.w);
        charToId.put('x', R.drawable.x);
        charToId.put('y', R.drawable.z);
        charToId.put('z', R.drawable.z);
    }
}
