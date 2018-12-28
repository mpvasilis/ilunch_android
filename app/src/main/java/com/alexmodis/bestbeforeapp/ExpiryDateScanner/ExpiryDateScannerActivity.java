package com.alexmodis.bestbeforeapp.ExpiryDateScanner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.OverlayView;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.CameraSource;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.CameraSourcePreview;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.FrameMetadata;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.common.GraphicOverlay;
import com.alexmodis.bestbeforeapp.ExpiryDateScannerUtil.ExpiryDayScanningProcessor;
import com.alexmodis.bestbeforeapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExpiryDateScannerActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CAMERA = 1001;
    public static final String KEY_CAMERA_PERMISSION_GRANTED = "CAMERA_PERMISSION_GRANTED";

    String TAG = "ExpiryDateScannerActivity";

    @BindView(R.id.barcodeOverlay)
    GraphicOverlay barcodeOverlay;
    @BindView(R.id.preview)
    CameraSourcePreview preview;
    @BindView(R.id.overlayView)
    OverlayView overlayView;

    ExpiryDayScanningProcessor barcodeScanningProcessor;

    private CameraSource mCameraSource = null;

    boolean isCalled;

    private Toast toast;

    boolean isAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expirydayscanner);

        ButterKnife.bind(this);


        if (preview != null)
            if (preview.isPermissionGranted(true, mMessageSender))
                new Thread(mMessageSender).start();
    }


    private void createCameraSource() {


        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();


        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

        barcodeScanningProcessor = new ExpiryDayScanningProcessor();
        barcodeScanningProcessor.setListener(barcodeScanningProcessor.gettListener());

        mCameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);


        startCameraSource();
    }


    private void startCameraSource() {

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d(TAG, "startCameraSource: " + code);

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, PERMISSION_REQUEST_CAMERA);
            dlg.show();
        }

        if (mCameraSource != null && preview != null && barcodeOverlay != null) {
            try {
                Log.d(TAG, "startCameraSource: ");
                preview.start(mCameraSource, barcodeOverlay);
            } catch (IOException e) {
                Log.d(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        } else
            Log.d(TAG, "startCameraSource: not started");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        preview.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (preview != null)
            preview.stop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isCalled = true;
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "handleMessage: ");

            if (preview != null)
                createCameraSource();

        }
    };

    private final Runnable mMessageSender = () -> {
        Log.d(TAG, "mMessageSender: ");
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CAMERA_PERMISSION_GRANTED, false);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    };

    public ExpiryDayScanningProcessor.ExpiryDateResultListener getBarcodeResultListener() {

        return new ExpiryDayScanningProcessor.ExpiryDateResultListener() {

            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull FirebaseVisionText texts, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                showToast(" detected " + texts.getText());

            }

            @Override
            public void onFailure(@NonNull Exception e) {

            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
    }

    public void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
