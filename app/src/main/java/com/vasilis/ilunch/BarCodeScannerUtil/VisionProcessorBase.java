
package com.vasilis.ilunch.BarCodeScannerUtil;

import android.graphics.Bitmap;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vasilis.ilunch.BarCodeScannerUtil.common.BitmapUtils;
import com.vasilis.ilunch.BarCodeScannerUtil.common.FrameMetadata;
import com.vasilis.ilunch.BarCodeScannerUtil.common.GraphicOverlay;
import com.vasilis.ilunch.BarCodeScannerUtil.common.VisionImageProcessor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;


public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;

    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")

    private FrameMetadata processingMetaData;

    public VisionProcessorBase() {
    }

    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, com.vasilis.ilunch.BarCodeScannerUtil.common.GraphicOverlay graphicOverlay) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay);
        }
    }

    @Override
    public void process(Bitmap bitmap, com.vasilis.ilunch.BarCodeScannerUtil.common.GraphicOverlay graphicOverlay) {
        detectInVisionImage(null /* bitmap */, FirebaseVisionImage.fromBitmap(bitmap), null,
                graphicOverlay);
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay) {

        // To create the FirebaseVisionImage

        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteBuffer(data, metadata);

        detectInVisionImage(
                bitmap, firebaseVisionImage, frameMetadata,
                graphicOverlay);
    }

    private void detectInVisionImage(
            final Bitmap originalCameraImage, FirebaseVisionImage image,
            final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(
                        results -> {
                            VisionProcessorBase.this.onSuccess(originalCameraImage, results,
                                    metadata,
                                    graphicOverlay);
                            processLatestImage(graphicOverlay);
                        })
                .addOnFailureListener(
                        VisionProcessorBase.this::onFailure);
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);


    protected abstract void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);
}
