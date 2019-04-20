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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.autodidacte.ui.camera.CameraSourcePreview;
import com.autodidacte.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
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
    private CameraSourcePreview preview;
    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private com.autodidacte.ui.camera.CameraSource cameraSource;

    // lettres
    com.autodidacte.ui.camera.CameraSource mCameraSource2 = null;
    SurfaceView mCameraView = null;
    TextView mTextView = null;
    int nTentativeCount = 0;
    int nFalseACount = 0;
    boolean _isLetterDetectionActive = true;
    // fin lettres

    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.

    private boolean _onTap = false;
    private int _flushDetectionBuffer = 0;
    private OcrDetectorProcessor m_Detector;
    boolean _mustFinish = false;

    private int kTimeBeforeNext = 20000;


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        GameEngine.returnToAlphabetActvity = true;
    }

    private class DetectionCallback implements OcrDetectorProcessor.ITextDetectCallback
    {
        public void ExecuteFirstTime()
        {

        }

        public void onReceiveDetections()
        {
            if(_mustFinish)
                OcrCaptureActivity.this.finish();
        }

        int _counter = 1;
        public void finish()
        {
            _mustFinish = true;

            do {
                m_Detector.release();
                OcrCaptureActivity.this.finish();
            }
            while(_counter-- > 0);
        }

        public boolean Execute(Vector<String> strings) {
            if(_mustFinish) {
                _mustFinish = false;
                finish();
            }
            else {

                if (_flushDetectionBuffer > 0) {
                    _flushDetectionBuffer--;
                    return true;
                }

                String rightWord = "";
                String existingWord = "";
                String possibleWord = "";
                String ExpectedWord = "";
                boolean trouve = false;
                ExpectedWord = GameEngine.wordFromLetter(GameEngine.currentItem().charAt(0));
                for (int i = 0; i < strings.size(); i++) {
                    String s = strings.elementAt(i);
                    if (GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
                        s = s.toLowerCase();
                        char c = s.charAt(0);
                        String wfl = GameEngine.wordFromLetter(c);
                        if (wfl.equals(s))
                            possibleWord = s;
                        if (s.equals(ExpectedWord)) {
                            rightWord = ExpectedWord;
                            m_Detector._waitingForDetection = false;
                            break;
                        }
                    }
                }

                if (GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
                    if (!rightWord.isEmpty()) {
                        GameEngine.onSuccess(rightWord);
                    } else if (!possibleWord.isEmpty()) {
                        GameEngine.onFail(possibleWord, ExpectedWord);
                    }
                }
            }
            return true;
        }
    }


    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        _mustFinish = false;

        GameEngine.setOcrCaptureActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
            setContentView(R.layout.ocr_capture);
            preview = (CameraSourcePreview) findViewById(R.id.preview);
            graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
        }
        else {
            setContentView(R.layout.ocr_capture2);
            mTextView = findViewById(R.id.text_view);
            mCameraView = findViewById(R.id.surfaceView);
        }

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot)
                createCameraSource(true, true);
            else
                createCameraSource2(true, true);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot) {
            Snackbar.make(graphicOverlay, "Tap to Speak. Pinch/Stretch to zoom",
                    Snackbar.LENGTH_LONG)
                    .show();
            m_Detector._waitingForDetection = true;
        }

        Utils.setAudioVolume(50, this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                OcrCaptureActivity.this.runOnUiThread(Timer_Tick);
            }
        }, kTimeBeforeNext);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            finish();
        }
    };

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
    boolean _justCreated = false;
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
        GameEngine.setOcrDetector(m_Detector);


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
                new com.autodidacte.ui.camera.CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(com.autodidacte.ui.camera.CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(15.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                        .build();

        //cameraSource.
        _justCreated = true;
    }

    boolean isExceptionMinMajLetter(char letter)
    {
        return letter == 'c' || letter == 'C' ||
                letter == 'k' || letter == 'K' ||
                letter == 'o' || letter == 'O' ||
                letter == 's' || letter == 'S' ||
                letter == 'u' || letter == 'U' ||
                letter == 'v' || letter == 'V' ||
                letter == 'w' || letter == 'W' ||
                letter == 'x' || letter == 'X' ||
                letter == 'z' || letter == 'Z';

    }


    private void createCameraSource2(boolean autoFocus, boolean useFlash) {

        mCameraView = findViewById(R.id.surfaceView);
        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource2 =
                    new com.autodidacte.ui.camera.CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_AUTO : null)
                    .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        int requestPermissionID = 101;
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(OcrCaptureActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource2.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource2.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    if(_mustFinish)
                        finish();
                    else {
                        if (!_isLetterDetectionActive)
                            return;
                        final SparseArray<TextBlock> items = detections.getDetectedItems();
                        if (items.size() != 0) {

                            mTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i = 0; i < items.size(); i++) {
                                        TextBlock item = items.valueAt(i);
                                        String foundString = item.getValue();
                                        if (foundString.length() == 1) {
                                            stringBuilder.append(item.getValue());
                                            stringBuilder.append("\n");

                                            String current = GameEngine.currentItem();
                                            String wantedLetter = current;
                                            if(GameEngine.getGameType() == GameEngine.GameType.eTrouverPremiereLettre)
                                                wantedLetter = current.substring(0, 1);
                                            char foundchar = foundString.charAt(0);
                                            if(Utils.isLetter(foundString.charAt(0)) ) {
                                                String modifiedFoundString = foundString;
                                                String modifiedCurrent = wantedLetter;
                                                if(isExceptionMinMajLetter(foundchar) ||
                                                        GameEngine.getGameType() == GameEngine.GameType.eTrouverPremiereLettre) {
                                                    modifiedFoundString = foundString.toLowerCase();
                                                    modifiedCurrent = wantedLetter.toLowerCase();
                                                }
                                                if (modifiedFoundString.equals(modifiedCurrent)) {
                                                    _isLetterDetectionActive = false;
                                                    GameEngine.onSuccess(wantedLetter);
                                                } else {
                                                    if(foundString.equals('A'))
                                                        nFalseACount++;
                                                    else
                                                        nTentativeCount++;
                                                    if ((nTentativeCount + nFalseACount / 2) > 4) {
                                                        nTentativeCount = 0;
                                                        nFalseACount = 0;
                                                        _isLetterDetectionActive = false;
                                                        GameEngine.onFail(foundString, wantedLetter);
                                                        break;
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    mTextView.setText(stringBuilder.toString());
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        int i = resultCode;
        _mustFinish = true;
        if(GameEngine.getGameType() == GameEngine.GameType.eTrouverLettre) {
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(Timer_Tick);
                }

            }, 1000);
        }
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
            if(GameEngine.getGameType() == GameEngine.GameType.eTrouverMot)
                createCameraSource(autoFocus, useFlash);
            else
                createCameraSource2(autoFocus, useFlash);
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
    int tapCount = 0;
    private boolean onTap(float rawX, float rawY) {
        // TODO: Speak the text when the user taps on screen.
        tapCount++;
        if(tapCount < 2) {
            GameEngine.onTap();
        }
        else {
            finish();
            tapCount = 0;
        }
        return true;
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
