package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ApprendreAlphabetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apprendre_alphabet);

        Button trouverLettre = findViewById(R.id.trouver_lettre_btn);
        Button lettreComme = findViewById(R.id.F_comme_btn);


        trouverLettre.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        OcrCaptureActivity._gameType = OcrCaptureActivity.GameType.eTrouverLettre;
                        Intent ocrCaptureActivity = new Intent(ApprendreAlphabetActivity.this, OcrCaptureActivity.class);
                        startActivity(ocrCaptureActivity);
                    }
                }
                );

        lettreComme.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        OcrCaptureActivity._gameType = OcrCaptureActivity.GameType.eLettreComme;
                        Intent ocrCaptureActivity = new Intent(ApprendreAlphabetActivity.this, OcrCaptureActivity.class);
                        startActivity(ocrCaptureActivity);
                    }
                }
        );
    }
}
