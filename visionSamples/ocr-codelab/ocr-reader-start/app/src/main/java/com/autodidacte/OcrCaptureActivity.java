/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.autodidacte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.autodidacte.ui.camera.CameraSource;
import com.autodidacte.ui.camera.CameraSourcePreview;
import com.autodidacte.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;

    private boolean _firstTime = true;
    private boolean _onTap = false;
    private boolean _onReturnBack = false;
    private int _flushDetectionBuffer = 0;


    private OcrDetectorProcessor m_Detector;

    Hashtable<Integer, ArrayList<String>> _notationWord;
    Hashtable<Integer, ArrayList<Character>> _notationLetter;
    Hashtable<Integer, ArrayList<Character>> _notationFirstLetter;
    private int _currentLevel = 0;
    private int _currentLetterLevel = 0;
    private int _currentWordIndex = -1;
    private int _currentLetterIndex = -1;
    private int _currentFirstLetterIndex = -1;
    private int _currentFirstLetterLevel = 0;

    int MAX_LEVEL = 3;
    int SUCCESS_ACTIVITY = 1;



    static Hashtable<Character, String> _letterToWord;
    static Hashtable<String, Character> _WordToLetter;


    static public void Sleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(Exception e)
        {
        }
    }

    private class DetectionCallback implements OcrDetectorProcessor.ITextDetectCallback
    {
        public void ExecuteFirstTime()
        {
            Sleep(1000);
            askNextItem();
        }



        public boolean Execute(Vector<String> strings) {
            if (_flushDetectionBuffer > 0) {
                _flushDetectionBuffer--;
                return true;
            }

            String rightWord = "";
            String existingWord = "";
            String possibleWord = "";
            String ExpectedWord = "";
            boolean trouve = false;
            ExpectedWord = wordFromLetter(currentItem().charAt(0));
            for (int i = 0; i < strings.size(); i++) {
                String s = strings.elementAt(i);
                if (_gameType == GameType.eLettreComme) {
                    s = s.toLowerCase();
                    char c = s.charAt(0);
                    String wfl = wordFromLetter(c);
                    if (wfl.equals(s))
                        possibleWord = s;
                    if (s.equals(ExpectedWord)) {
                        rightWord = ExpectedWord;
                        m_Detector._waitingForDetection = false;
                        break;
                    }
                }
                else if(_gameType == GameType.eTrouverLettre) {
                    if(s.length() == 1) {
                        String sCurrent = String.valueOf(currentItem());
                        if (s.equals(sCurrent)) {
                            m_Detector._waitingForDetection = false;
                            trouve = true;
                            break;
                        }
                    }
                }
            }

            if (_gameType == GameType.eLettreComme) {
                if (!rightWord.isEmpty()) {
                    onSuccess(rightWord);
                } else if (!possibleWord.isEmpty()) {
                    onFail(possibleWord, ExpectedWord);
                }
            }
            else if(_gameType == GameType.eTrouverLettre) {
                if(trouve)
                    onLetterSuccess(currentItem().charAt(0));
            }

            return true;
        }
    }

    void onLetterSuccess(char c)
    {
        String sentence = "Bravo, tu as trouvé la bonne lettre ! ";

        Intent ocrCaptureActivity = new Intent(OcrCaptureActivity.this, SuccessActivity.class);
        ocrCaptureActivity.putExtra("sentence", sentence);
        ocrCaptureActivity.putExtra("result", "success");
        startActivityForResult(ocrCaptureActivity, SUCCESS_ACTIVITY);

    }

    void onSuccess(String wordFound)
    {
        boolean version1 = false;
        boolean version2 = false;
        boolean version3 = true;
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String level = prefs.getString(wordFound, "");

        Integer nLevel = Integer.parseInt(level) + 1;
        level = Integer.toString(nLevel);
        editor.putString(wordFound, level);
        editor.commit();
        String test = prefs.getString(wordFound, "");

        String sentence = "Bravo, " + wordFound + " commence bien par " + currentItem() + " ! ";
        Intent ocrCaptureActivity = new Intent(OcrCaptureActivity.this, SuccessActivity.class);
        ocrCaptureActivity.putExtra("sentence", sentence);
        ocrCaptureActivity.putExtra("result", "success");
        startActivityForResult(ocrCaptureActivity, SUCCESS_ACTIVITY);
    }

    void onFail(String wrongWord, String ExpectedWord)
    {
        m_Detector._waitingForDetection = false;
        String sentence = "Tu tes trompé, " + wrongWord + " ne commence pas par, " + currentItem() +
                          ", le bon mot etait " + ExpectedWord;
        Intent ocrCaptureActivity = new Intent(OcrCaptureActivity.this, SuccessActivity.class);
        ocrCaptureActivity.putExtra("sentence", sentence);
        ocrCaptureActivity.putExtra("result", "error");
        startActivityForResult(ocrCaptureActivity, SUCCESS_ACTIVITY);
    }

    //@override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == SUCCESS_ACTIVITY) {
            tts.playSilentUtterance(2000, TextToSpeech.QUEUE_ADD, "silence");
            _onReturnBack = true;
            askNextItem();
            _onReturnBack = false;
            Sleep(1000);
            m_Detector._waitingForDetection = true;
            if(_gameType == GameType.eLettreComme)
                _flushDetectionBuffer = 3;
            else if(_gameType == GameType.eTrouverLettre)
                _flushDetectionBuffer = 1;
        }
    }

    boolean isIntoWordList(String word)
    {
        return _letterToWord.contains(word);
    }

    String wordFromLetter(char c)
    {
        if(c < 'a')
            c = (char)(c + ('a' - 'A'));
        if(_letterToWord.containsKey(c))
            return _letterToWord.get(c);
        return "";
    }

    public enum GameType
    {
        eTrouverLettre,
        eTrouver1ereLettre,
        eLettreComme;
    }

    public static GameType _gameType;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setContentView(R.layout.ocr_capture);
        _currentWordIndex = -1;
        _currentLetterIndex = -1;
        _currentLevel = 0;

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean flush = false;
        if(flush) {
            editor.clear();
            editor.commit();
        }

        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = true;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(graphicOverlay, "Tap to Speak. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();

        // TODO: Set up the Text To Speech engine.
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("TTS", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.FRANCE);
                        } else {
                            Log.d("TTS", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);

        if(_letterToWord == null)
            _letterToWord = new Hashtable<Character, String>();

        if(_letterToWord.size() == 0) {
            initLetterToWord();
        }

        initNotationWords();
        initNotationLetter(_notationLetter);
        initNotationLetter(_notationFirstLetter);
        _firstTime = true;

    }

    private void initNotationWords()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String init = preferences.getString("init", null);
        Iterator<Character> it = _letterToWord.keySet().iterator();
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            while(it.hasNext()) {
                String value = _letterToWord.get(it.next());
                editor.putString(value, "0");
            }

            editor.putString("init", "init");
            editor.apply();
        }

        _notationWord = new Hashtable<Integer, ArrayList<String>>();

        it = _letterToWord.keySet().iterator();
        while(it.hasNext()) {
            String value = _letterToWord.get(it.next());
            String level = preferences.getString(value, "");
            int nLevel = Integer.parseInt(level);
            if(_notationWord.containsKey(nLevel) == false)
                _notationWord.put(nLevel, new ArrayList<String>());
            _notationWord.get(nLevel).add(value);
        }
    }

    private void initNotationLetter(Hashtable<Integer, ArrayList<Character>> letters)
    {
        letters = new Hashtable<Integer, ArrayList<Character>>();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String init = preferences.getString("iniLetters", null);
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            for(int i = 0; i < 26; i++) {
                char letter = (char)('A' + i);
                String sLetter = String.valueOf(letter);
                editor.putString(sLetter, "0");
                letter = (char)('a' + i);
                String letterMinus = String.valueOf(letter);
                editor.putString(letterMinus, "0");
            }

            editor.putString("initLetter", "init");
            editor.apply();
        }

        initNotationLetterArray('a', letters);
        initNotationLetterArray('A', letters);
    }

    void initNotationLetterArray(Character firstChar, Hashtable<Integer, ArrayList<Character>> letters)
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        for(int i = 0; i < 26; i++) {
            char letter = (char)(i + firstChar);
            String value = String.valueOf(letter);
            String level = preferences.getString(value, "");
            int nLevel = Integer.parseInt(level);
            if(letters.containsKey(nLevel) == false)
                letters.put(nLevel, new ArrayList<Character>());
            letters.get(nLevel).add(letter);
        }
    }

    private void initLetterToWord()
    {
        _letterToWord.put('a', "avion");
        _letterToWord.put('b', "banane");
        _letterToWord.put('c', "carotte");
        _letterToWord.put('d', "dinosaure");
        _letterToWord.put('e', "éléphant");
        _letterToWord.put('f', "fraise");
        _letterToWord.put('g', "girafe");
        _letterToWord.put('h', "hérisson");
        _letterToWord.put('i', "igloo");
        _letterToWord.put('j', "jus");
        _letterToWord.put('k', "kangourou");
        _letterToWord.put('l', "lion");
        _letterToWord.put('m', "mangue");
        _letterToWord.put('n', "nuage");
        _letterToWord.put('o', "orange");
        _letterToWord.put('p', "pomme");
        _letterToWord.put('q', "quatre");
        _letterToWord.put('r', "robot");
        _letterToWord.put('s', "soleil");
        _letterToWord.put('t', "tigre");
        _letterToWord.put('u', "ustensiles");
        _letterToWord.put('v', "voiture");
        _letterToWord.put('w', "wagon");
        _letterToWord.put('x', "xylophone");
        _letterToWord.put('y', "yaourt");
        _letterToWord.put('z', "zèbre");
    }

    private void initWordToLetter()
    {
        _WordToLetter.put("avion", 'a');
        _WordToLetter.put("banane", 'b');
        _WordToLetter.put("carotte", 'c');
        _WordToLetter.put("dinosaure", 'd');
        _WordToLetter.put("éléphant", 'e');
        _WordToLetter.put("fraise", 'f');
        _WordToLetter.put("girafe", 'g');
        _WordToLetter.put("hérisson", 'h');
        _WordToLetter.put("igloo", 'i');
        _WordToLetter.put("jus", 'j');
        _WordToLetter.put("kangourou", 'k');
        _WordToLetter.put("lion", 'l');
        _WordToLetter.put("mangue", 'm');
        _WordToLetter.put("nuage", 'n');
        _WordToLetter.put("orange", 'o');
        _WordToLetter.put("pomme", 'p');
        _WordToLetter.put("quatre", 'q');
        _WordToLetter.put("robot", 'r');
        _WordToLetter.put("soleil", 's');
        _WordToLetter.put("tigre", 't');
        _WordToLetter.put("ustensiles", 'u');
        _WordToLetter.put("voiture", 'v');
        _WordToLetter.put("wagon", 'w');
        _WordToLetter.put("xylophone", 'x');
        _WordToLetter.put("yaourt", 'y');
        _WordToLetter.put("zèbre", 'z');
    }

    private String currentItem()
    {
        if(_gameType == GameType.eLettreComme)
            return  _notationWord.get(_currentLevel).get(_currentWordIndex).substring(0, 1);
        else if(_gameType == GameType.eTrouverLettre)
            return _notationLetter.get(_currentLetterLevel).get(_currentLetterIndex).toString();
        else if(_gameType == GameType.eTrouver1ereLettre)
            return _notationFirstLetter.get(_currentFirstLetterLevel).get(_currentFirstLetterIndex).toString();
        return "";
    }

    private String nextItem()
    {
        if(_gameType == GameType.eLettreComme) {
            _currentWordIndex++;
            if (_currentWordIndex >= _notationWord.get(_currentLevel).size()) {
                do {
                    _currentLevel++;
                }
                while (_notationWord.get(_currentLevel).size() == 0 || _currentLevel < MAX_LEVEL);
                _currentWordIndex = 0;
            }
        }
        else if(_gameType == GameType.eTrouverLettre) {
            _currentLetterIndex++;
            if (_currentLetterIndex >= _notationLetter.get(_currentLetterLevel).size()) {
                do {
                    _currentLetterLevel++;
                }
                while (_notationWord.get(_currentLetterLevel).size() == 0 || _currentLetterLevel < MAX_LEVEL);
                _currentLetterIndex = 0;
            }
        }
        else if(_gameType == GameType.eTrouver1ereLettre) {
            _currentFirstLetterIndex++;
            if (_currentFirstLetterIndex >= _notationFirstLetter.get(_currentFirstLetterLevel).size()) {
                do {
                    _currentFirstLetterLevel++;
                }
                while (_notationFirstLetter.get(_currentFirstLetterLevel).size() == 0 || _currentFirstLetterLevel < MAX_LEVEL);
                _currentFirstLetterIndex = 0;
            }
        }
        return currentItem();
    }

    public void askNextItem()
    {
        tts.setLanguage(Locale.FRANCE);
        String c;
        if(!_onTap){
            m_Detector._waitingForDetection = false;
            c = nextItem();
        }
        else
            c = currentItem();

        tts.setSpeechRate(0.8f);
        String sentence = "";

        if(_gameType == GameType.eLettreComme) {
            if (_firstTime) {
                sentence = "Bonjour, quel mot commence par la lettre, " + c;
                _firstTime = false;
            } else if (_onTap)
                sentence += "Quel mot commence par la lettre, " + c;
            else
                sentence = "Lettre suivante, quel mot commence par, " + c;
            if(c == "y" || c == "Y")
                sentence += " grec ";
        }
        else if(_gameType == GameType.eTrouverLettre){
            if (_firstTime) {
                sentence = "Bonjour, trouve moi la lettre, " + c;
                if(c == "y" || c == "Y")
                    sentence += " grec ";
                if(isLow(c.charAt(0)))
                    sentence += " minuscule";
                else
                    sentence += " majuscule";
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la lettre, " + c;
            else
                sentence = "Lettre suivante, trouve moi la lettre, " + c + " ?";
        }
        else if(_gameType == GameType.eTrouver1ereLettre){
            if (_firstTime) {
                sentence = "Bonjour, par quelle lettre commence le mot " + c;
                if(c == "y" || c == "Y")
                    sentence += " grec ";
                if(isLow(c.charAt(0)))
                    sentence += " minuscule";
                else
                    sentence += " majuscule";
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la lettre, " + c;
            else
                sentence = "Lettre suivante, trouve moi la lettre, " + c + " ?";
        }

        tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
        if (!_onTap && !_onReturnBack) {
            Sleep(4000);
            m_Detector._waitingForDetection = true;
        }
    }

    boolean isLow(char c)
    {
        return c >= 'a';
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // Create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Set the TextRecognizer's Processor.
        m_Detector = new OcrDetectorProcessor(graphicOverlay);
        DetectionCallback onWordDetected = new DetectionCallback();
        m_Detector.setDetectionCallback(onWordDetected);
        textRecognizer.setProcessor(m_Detector);


        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Create the cameraSource using the TextRecognizer.
        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(15.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                        .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }

    }

    /**
     * onTap is called to speak the tapped TextBlock, if any, out loud.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the tap was on a TextBlock
     */
    private boolean onTap(float rawX, float rawY) {
        // TODO: Speak the text when the user taps on screen.
        OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d(TAG, "text data is being spoken! " + text.getValue());
                // TODO: Speak the string.
                tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        _onTap = true;
        askNextItem();
        _onTap = false;
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (cameraSource != null) {
                cameraSource.doZoom(detector.getScaleFactor());
            }
        }
    }
}
