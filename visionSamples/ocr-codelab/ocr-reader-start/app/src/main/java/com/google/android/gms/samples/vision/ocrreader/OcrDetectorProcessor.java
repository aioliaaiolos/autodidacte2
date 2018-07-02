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
package com.google.android.gms.samples.vision.ocrreader;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.Vector;


/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * TODO: Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
 */


public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private boolean _debugInfos = false;

    private boolean _justOpened = true;

    public interface ITextDetectCallback
    {
        public boolean Execute(Vector<String> strings);
        public void ExecuteFirstTime();
    }

    private ITextDetectCallback m_detectionCallback = null;


    private GraphicOverlay<OcrGraphic> graphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    public boolean _waitingForDetection = false;

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        if (_justOpened) {
            m_detectionCallback.ExecuteFirstTime();
            _justOpened = false;
        }

        if(_waitingForDetection) {
            graphicOverlay.clear();
            SparseArray<TextBlock> items = detections.getDetectedItems();
            if (items.size() > 0) {
                Vector<String> strings = new Vector<String>();
                for (int i = 0; i < items.size(); ++i) {
                    TextBlock item = items.valueAt(i);
                    if (item != null && item.getValue() != null) {
                        strings.add(item.getValue());
                        Log.d("Processor", "Text detected! " + item.getValue());
                        OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                        if(_debugInfos == true)
                            graphicOverlay.add(graphic);
                    }
                }
                if (m_detectionCallback != null)
                    m_detectionCallback.Execute(strings);
            }
        }
    }

    @Override
    public void release() {
        graphicOverlay.clear();
    }

    public void setDetectionCallback(ITextDetectCallback callback)
    {
        m_detectionCallback = callback;
    }

    // TODO:  Once this implements Detector.Processor<TextBlock>, implement the abstract methods.
}
