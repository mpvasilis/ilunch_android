package com.vasilis.ilunch.BarcodeScanner;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.vasilis.ilunch.BarCodeScannerUtil.BarcodeScanningProcessor;
import com.vasilis.ilunch.BarCodeScannerUtil.BarcodeScanningProcessor.BarcodeResultListener;
import com.vasilis.ilunch.BarCodeScannerUtil.OverlayView;
import com.vasilis.ilunch.BarCodeScannerUtil.common.CameraSource;
import com.vasilis.ilunch.BarCodeScannerUtil.common.CameraSourcePreview;
import com.vasilis.ilunch.BarCodeScannerUtil.common.FrameMetadata;
import com.vasilis.ilunch.BarCodeScannerUtil.common.GraphicOverlay;
import com.vasilis.ilunch.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BarcodeScannerActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CAMERA = 1001;
    public static final String KEY_CAMERA_PERMISSION_GRANTED = "CAMERA_PERMISSION_GRANTED";

    String TAG = "BarcodeScannerActivity";

    @BindView(R.id.barcodeOverlay)
    GraphicOverlay barcodeOverlay;
    @BindView(R.id.preview)
    CameraSourcePreview preview;
    @BindView(R.id.overlayView)
    OverlayView overlayView;

    BarcodeScanningProcessor barcodeScanningProcessor;

    private CameraSource mCameraSource = null;

    boolean isCalled;

    private Toast toast;

    boolean isAdded = false;
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        queue = Volley.newRequestQueue(this);  // this = context

        ButterKnife.bind(this);


        if (preview != null)
            if (preview.isPermissionGranted(true, mMessageSender))
                new Thread(mMessageSender).start();
    }


    private void createCameraSource() {

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);


        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

        barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
        barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());

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

    public BarcodeResultListener getBarcodeResultListener() {

        return new BarcodeResultListener() {
            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionBarcode> barcodes, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                Log.d(TAG, "onSuccess: " + barcodes.size());

                for (FirebaseVisionBarcode barCode : barcodes) {

                    //showToast("Barcode detected " + barCode.getRawValue());
                    processBarcode(barCode.getRawValue());
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
        if (message.equals("3")) {
            message = "Λυπούμαστε αλλά όπως φαίνεται έχετε φάει!'";
        } else if (message.equals("2")) {
            message = "Λυπούμαστε, η συνδρομή σας έχει λήξει, επικοινωνήστε με τον υπεύθυνο της λέσχης για ανανέωση της συνδρομής σας.";
        } else if (message.equals("1")) {
            message = "Η συνδρομή σας δεν περιελαμβάνει αυτό τον τύπο γεύματος!";
        } else if (message.equals("0")) {
            message = "Δεκτό!";
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    public void processBarcode(String barcode) {
        Log.d("barcode", barcode);

        String url = "https://zafora.ece.uowm.gr/~ictest01041/ilunch_v10/public/api/validateCustomer/" + barcode + "?apiKey=s@r";
        if (barcode.contains("[a-zA-Z]+") == false && barcode.length() < 15)
            url = "https://zafora.ece.uowm.gr/~ictest01041/ilunch_v10/public/api/ckeckFreeSitisi/" + barcode + "?apiKey=s@r";
        Log.d("url", url);
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // response
                    Log.d("Response", response);
                    showToast(response);
                },
                error -> {
                    toast = Toast.makeText(getApplicationContext(), "Δεν είναι δυνατή η σύνδεση με τον διακομιστή ή δεν σαρώθηκε έγκυρος QR κωδικός", Toast.LENGTH_LONG);
                    toast.show();
                    Log.d("Error.Response", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        queue.add(postRequest);


    }
}
