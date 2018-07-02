package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Locale;

public class SuccessActivity extends AppCompatActivity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        ImageButton resultButton = (ImageButton)findViewById(R.id.newbutton);
        Intent currentIntent = getIntent();
        String result = currentIntent.getStringExtra("result");
        if(result.equals("success"))
            resultButton.setBackgroundResource(R.drawable.valider);
        else
            resultButton.setBackgroundResource(R.drawable.error);

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
                        } else {
                            Log.d("TTS", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
        tts.setLanguage(Locale.FRANCE);

        OcrCaptureActivity.Sleep(1000);


        ///
    }

    public void clickNew(View v)
    {
        //Toast.makeText(this, "Show some text on the screen.", Toast.LENGTH_LONG).show();
        finish();
       // Intent capture = new Intent(OcrCaptureActivity.this, SuccessActivity.class)
    }
}
