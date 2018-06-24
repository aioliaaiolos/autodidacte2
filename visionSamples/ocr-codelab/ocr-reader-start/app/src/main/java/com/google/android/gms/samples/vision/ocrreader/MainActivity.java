package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mApprendreAphabet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApprendreAphabet = (Button)findViewById(R.id.activity_main_alphabet_txt);

        mApprendreAphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Intent apprendreAlphabet = new Intent(MainActivity.this, ApprendreAlphabetActivity.class);
                startActivity(apprendreAlphabet);
            }
        });



    }
}
