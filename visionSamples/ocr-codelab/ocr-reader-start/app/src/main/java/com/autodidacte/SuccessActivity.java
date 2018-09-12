package com.autodidacte;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import java.util.Hashtable;

public class SuccessActivity extends AppCompatActivity {

    private TextToSpeech tts;
    static Hashtable<Character, Integer> charToWordId = null;
    static Hashtable<Character, Integer> charToLetterId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(charToWordId == null)
        {
            initMapping();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        ImageButton resultButton = (ImageButton)findViewById(R.id.newbutton);
        Intent currentIntent = getIntent();
        String result = currentIntent.getStringExtra("result");
        String currentItem = currentIntent.getStringExtra("currentItem");

        if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
            if(currentItem.length() > 0) {
                Character c = currentItem.charAt(0);
                Object o = charToWordId.get(c);
                if(o != null) {
                    int res = (int)o;
                    resultButton.setBackgroundResource(res);
                }
            }
        }
        else if(GameEngine.getGameType() == GameEngine.GameType.eTrouverLettre) {
            if(currentItem.length() > 0) {
                Character c = currentItem.charAt(0);
                Object o = charToLetterId.get(c);
                if(o != null) {
                    int res = (int)o;
                    resultButton.setBackgroundResource(res);
                }
            }
        }
        else if(GameEngine.getGameType() == GameEngine.GameType.eTrouverPremiereLettre) {
            if(currentItem.length() > 0) {
                Character c = currentItem.charAt(0);
                Object o = charToLetterId.get(c);
                if(o != null) {
                    int res = (int)o;
                    resultButton.setBackgroundResource(res);
                }
            }
        }
        /*
        else
            resultButton.setBackgroundResource(R.drawable.valider);*/

        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("TTS", "Text to speech engine started successfully.");
                            tts.setLanguage(GameEngine.currentLanguage());
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
        tts.setLanguage(GameEngine.currentLanguage());

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
        charToWordId = new Hashtable<Character, Integer>();
        charToWordId.put('a', R.drawable.a);
        charToWordId.put('b', R.drawable.b);
        charToWordId.put('c', R.drawable.c);
        charToWordId.put('d', R.drawable.d);
        charToWordId.put('e', R.drawable.e);
        charToWordId.put('f', R.drawable.f);
        charToWordId.put('g', R.drawable.g);
        charToWordId.put('h', R.drawable.h);
        charToWordId.put('i', R.drawable.i);
        charToWordId.put('j', R.drawable.j);
        charToWordId.put('k', R.drawable.k);
        charToWordId.put('l', R.drawable.l);
        charToWordId.put('m', R.drawable.m);
        charToWordId.put('n', R.drawable.n);
        charToWordId.put('o', R.drawable.o);
        charToWordId.put('p', R.drawable.p);
        charToWordId.put('q', R.drawable.q);
        charToWordId.put('r', R.drawable.r);
        charToWordId.put('s', R.drawable.s);
        charToWordId.put('t', R.drawable.t);
        charToWordId.put('u', R.drawable.u);
        charToWordId.put('v', R.drawable.v);
        charToWordId.put('w', R.drawable.w);
        charToWordId.put('x', R.drawable.x);
        charToWordId.put('y', R.drawable.z);
        charToWordId.put('z', R.drawable.z);

        charToLetterId = new Hashtable<Character, Integer>();
        charToLetterId.put('a', R.drawable.amin);
        charToLetterId.put('b', R.drawable.bmin);
        charToLetterId.put('c', R.drawable.cmin);
        charToLetterId.put('d', R.drawable.dmin);
        charToLetterId.put('e', R.drawable.emin);
        charToLetterId.put('f', R.drawable.fmin);
        charToLetterId.put('g', R.drawable.gmin);
        charToLetterId.put('h', R.drawable.hmin);
        charToLetterId.put('i', R.drawable.imin);
        charToLetterId.put('j', R.drawable.jmin);
        charToLetterId.put('k', R.drawable.kmin);
        charToLetterId.put('l', R.drawable.lmin);
        charToLetterId.put('m', R.drawable.mmin);
        charToLetterId.put('n', R.drawable.nmin);
        charToLetterId.put('o', R.drawable.omin);
        charToLetterId.put('p', R.drawable.pmin);
        charToLetterId.put('q', R.drawable.qmin);
        charToLetterId.put('r', R.drawable.rmin);
        charToLetterId.put('s', R.drawable.smin);
        charToLetterId.put('t', R.drawable.tmin);
        charToLetterId.put('u', R.drawable.umin);
        charToLetterId.put('v', R.drawable.vmin);
        charToLetterId.put('w', R.drawable.wmin);
        charToLetterId.put('x', R.drawable.xmin);
        charToLetterId.put('y', R.drawable.ymin);
        charToLetterId.put('z', R.drawable.zmin);

        charToLetterId.put('A', R.drawable.amaj);
        charToLetterId.put('B', R.drawable.bmaj);
        charToLetterId.put('C', R.drawable.cmaj);
        charToLetterId.put('D', R.drawable.dmaj);
        charToLetterId.put('E', R.drawable.emaj);
        charToLetterId.put('F', R.drawable.fmaj);
        charToLetterId.put('G', R.drawable.gmaj);
        charToLetterId.put('H', R.drawable.hmaj);
        charToLetterId.put('I', R.drawable.imaj);
        charToLetterId.put('J', R.drawable.jmaj);
        charToLetterId.put('K', R.drawable.kmaj);
        charToLetterId.put('L', R.drawable.lmaj);
        charToLetterId.put('M', R.drawable.mmaj);
        charToLetterId.put('N', R.drawable.nmaj);
        charToLetterId.put('O', R.drawable.omaj);
        charToLetterId.put('P', R.drawable.pmaj);
        charToLetterId.put('Q', R.drawable.qmaj);
        charToLetterId.put('R', R.drawable.rmaj);
        charToLetterId.put('S', R.drawable.smaj);
        charToLetterId.put('T', R.drawable.tmaj);
        charToLetterId.put('U', R.drawable.umaj);
        charToLetterId.put('V', R.drawable.vmaj);
        charToLetterId.put('W', R.drawable.wmaj);
        charToLetterId.put('X', R.drawable.xmaj);
        charToLetterId.put('Y', R.drawable.ymaj);
        charToLetterId.put('Z', R.drawable.zmaj);
    }
}
