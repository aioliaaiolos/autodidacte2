package com.autodidacte;

import com.google.android.gms.vision.Detector;



public class OcrDetectorCharacter implements Detector.Processor<Character> {

    @Override
    public void receiveDetections(Detector.Detections<Character> detections) {

    }

    @Override
    public void release() {

    }
}