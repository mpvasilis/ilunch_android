package com.alexmodis.bestbeforeapp.ExpiryDateScanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    ExpiryDayScanningProcessor expirydatescannerproccessor;

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

        expirydatescannerproccessor = new ExpiryDayScanningProcessor();
        expirydatescannerproccessor.setListener(gettListener());

        mCameraSource.setMachineLearningFrameProcessor(expirydatescannerproccessor);


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


    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

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

    public ExpiryDayScanningProcessor.ExpiryDateResultListener gettListener() {

        return new ExpiryDayScanningProcessor.ExpiryDateResultListener() {

            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull FirebaseVisionText texts, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                if (isValidDate(texts.getText())) {
                    showToast("Detected " + texts.getText());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("expiryday_detected", texts.getText());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }

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

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

}
