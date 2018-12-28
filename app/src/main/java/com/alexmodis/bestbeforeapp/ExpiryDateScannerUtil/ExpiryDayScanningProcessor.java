
package com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.CameraImageGraphic;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.FrameMetadata;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;


import java.io.IOException;
import java.util.List;

public class ExpiryDayScanningProcessor extends VisionProcessorBase<FirebaseVisionText> {

    ExpiryDateResultListener expiryDateResultListener;


    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;

    public ExpiryDayScanningProcessor() {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    public ExpiryDateResultListener gettListener() {
        return expiryDateResultListener;
    }

    public void setListener(ExpiryDateResultListener barcodeResultListener) {
        this.expiryDateResultListener = barcodeResultListener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                    originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay,
                            elements.get(k));
                    graphicOverlay.add(textGraphic);
                }
            }
        }
        graphicOverlay.postInvalidate();

        if (expiryDateResultListener != null)
            expiryDateResultListener.onSuccess(originalCameraImage, results, frameMetadata, graphicOverlay);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }

    public interface ExpiryDateResultListener {
        void onSuccess(
                @Nullable Bitmap originalCameraImage,
                @NonNull FirebaseVisionText texts,
                @NonNull FrameMetadata frameMetadata,
                @NonNull GraphicOverlay graphicOverlay);

        void onFailure(@NonNull Exception e);
    }
}
