package com.vasilis.ilunch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.palaima.smoothbluetooth.Device;
import io.palaima.smoothbluetooth.SmoothBluetooth;


public class BluetoothActivity extends AppCompatActivity {

    public static final int ENABLE_BT__REQUEST = 1;

    private SmoothBluetooth mSmoothBluetooth;

    private Button mScanButton;

    private TextView mStateTv;

    private TextView mDeviceTv;

    private Button mPairedButton;

    private Button mDisconnectButton;

    private LinearLayout mConnectionLayout;

    private LinearLayout functions;


    private RelativeLayout mDiscLayout;

    private EditText mMessageInput;

    private Button mSendButton;

    private Button upload;
    private Button databtn;

    private Button time;

    private CheckBox mCRLFBox;

    private List<Integer> mBuffer = new ArrayList<>();
    private List<String> mResponseBuffer = new ArrayList<>();

    private ArrayAdapter<String> mResponsesAdapter;

    private int totallines;
    private int linesrecieved;
    boolean success=false;


    private SmoothBluetooth.Listener mListener = new SmoothBluetooth.Listener() {
        @Override
        public void onBluetoothNotSupported() {
            Toast.makeText(BluetoothActivity.this, "Bluetooth not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onBluetoothNotEnabled() {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, ENABLE_BT__REQUEST);
        }

        @Override
        public void onConnecting(Device device) {
            mStateTv.setText("Σύνδεση σε: ");
            mDeviceTv.setText(device.getName());
        }

        @Override
        public void onConnected(Device device) {
            mStateTv.setText("Συνδέθηκε σε: ");
            mDeviceTv.setText(device.getName());
            mConnectionLayout.setVisibility(View.GONE);
            mDiscLayout.setVisibility(View.VISIBLE);
            mDisconnectButton.setVisibility(View.VISIBLE);
            functions.setVisibility(View.VISIBLE);

            mSendButton.setEnabled(true);
            mSmoothBluetooth.send("C", mCRLFBox.isChecked());


        }

        @Override
        public void onDisconnected() {
            mStateTv.setText("Δεν υπάρχει σύνδεση σε τερματικό.");
            mDeviceTv.setText("");
            mDisconnectButton.setVisibility(View.GONE);
            mDiscLayout.setVisibility(View.GONE);
            mConnectionLayout.setVisibility(View.VISIBLE);
            mSendButton.setEnabled(false);
            functions.setVisibility(View.GONE);


        }

        @Override
        public void onConnectionFailed(Device device) {
            mStateTv.setText("Δεν υπάρχει σύνδεση σε τερματικό.");
            mDeviceTv.setText("");
            Toast.makeText(BluetoothActivity.this, "Η σύνδεση σε  " + device.getName() + " απέτυχε", Toast.LENGTH_SHORT).show();
            if (device.isPaired()) {
                mSmoothBluetooth.doDiscovery();
            }
        }

        @Override
        public void onDiscoveryStarted() {
            Toast.makeText(BluetoothActivity.this, "Γίνετε αναζήτηση συσκευών....", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDiscoveryFinished() {

        }

        @Override
        public void onNoDevicesFound() {
            Toast.makeText(BluetoothActivity.this, "Δεν βρέθηκε κάποια συσκευή", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDevicesFound(final List<Device> deviceList,
                                   final SmoothBluetooth.ConnectionCallback connectionCallback) {

            final MaterialDialog dialog = new MaterialDialog.Builder(BluetoothActivity.this)
                    .title("Συσκευές")
                    .adapter(new DevicesAdapter(BluetoothActivity.this, deviceList), null)
                    .build();

            ListView listView = dialog.getListView();
            if (listView != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        connectionCallback.connectTo(deviceList.get(position));
                        dialog.dismiss();
                    }

                });
            }

            dialog.show();

        }

        int counter = 0;
        StringBuilder sb;
        @Override
        public void onDataReceived(int data) {
            System.out.println((char) data);
            mBuffer.add(data);

            char datar = (char) data;
            System.out.println(datar);
            Log.d("charrecieved", String.valueOf(datar));

            if (datar == '\n' && !mBuffer.isEmpty()) {
                //if (data == 0x0D && !mBuffer.isEmpty() && mBuffer.get(mBuffer.size()-2) == 0xA0) {
                sb = new StringBuilder();
                for (int integer : mBuffer) {
                    sb.append((char) integer);
                }
                mBuffer.clear();
                String s = sb.toString();
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ',') {
                        Log.d("counter", String.valueOf(counter));

                        counter++;
                    }
                }
                if (counter >= 1) {
                    writefile(sb.toString(), mDeviceTv.getText().toString());
                    linesrecieved++;
                    Log.d("linesrecieved", String.valueOf(linesrecieved));
                    Log.d("linesrecieved", sb.toString());

                    counter = 0;
                } else if (sb.toString().contains("success")) {
                    // success=true;
                    mResponseBuffer.add(0, "Επιτυχής μεταφορά δεδομένων. Σύνολο: " + linesrecieved);
                    mResponsesAdapter.notifyDataSetChanged();
                    Toast.makeText(BluetoothActivity.this, "Επιτυχής μεταφορά δεδομένων.", Toast.LENGTH_LONG).show();
                    //cpuwakelock(false);
                    //handler.removeCallbacks(runnable);
                    linesrecieved = 0;
                } else {
                    try {
                        Calendar mydate = Calendar.getInstance();
                        //mydate.setTimeInMillis( Integer.parseInt(sb.toString().split(",")[0]));
                        mResponseBuffer.add(0, mydate.get(Calendar.DAY_OF_MONTH) + "." + mydate.get(Calendar.MONTH) + "." + mydate.get(Calendar.YEAR) + " - " + sb.toString().split(",")[1].replaceAll("\n", "").replaceAll("1", "Bad").replaceAll("3", "Moderate").replaceAll("4", "Good").replaceAll("5", "Great"));
                        mResponsesAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        mResponseBuffer.add(0, sb.toString().replaceAll("\n", ""));
                        mResponsesAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    };

    public String getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getTimeZone("Europe/Athens");
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }


    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if (file.getName().endsWith(".log")) {
                    inFiles.add(file);
                }
            }
        }

        return inFiles;
    }

    ArrayList ratingsDataarray = new ArrayList<ratingsData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Διαχείρηση τερματικών");
        }

        // mSmoothBluetooth = new SmoothBluetooth(this);
        mSmoothBluetooth = new SmoothBluetooth(getApplicationContext());


        mSmoothBluetooth.setListener(mListener);

        ListView responseListView = (ListView) findViewById(R.id.responses);
        mResponsesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mResponseBuffer);
        responseListView.setAdapter(mResponsesAdapter);

        mCRLFBox = (CheckBox) findViewById(R.id.carrage);
        mMessageInput = (EditText) findViewById(R.id.message);

        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSmoothBluetooth.send("D", mCRLFBox.isChecked());
                mMessageInput.setText("");
            }
        });

        time = (Button) findViewById(R.id.time);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = tsLong.toString();
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);

                mSmoothBluetooth.send("T", mCRLFBox.isChecked());
                mSmoothBluetooth.send("," + year + "," + month + "," + day + "," + hour + "," + minute, mCRLFBox.isChecked());
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                Toast.makeText(BluetoothActivity.this, "Setting time to: " + currentDateTimeString, Toast.LENGTH_SHORT).show();

            }
        });

        mSendButton.setEnabled(false);

        mDisconnectButton = (Button) findViewById(R.id.disconnect);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(BluetoothActivity.this)
                        .setTitle("Disconnect")
                        .setMessage("Are you sure you want to disconnect? Please, do not disconnect if a transfer is in progress.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mSmoothBluetooth.disconnect();
                                mResponseBuffer.clear();
                                mResponsesAdapter.notifyDataSetChanged();                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        mConnectionLayout = (LinearLayout) findViewById(R.id.connection);
        functions = (LinearLayout) findViewById(R.id.functions);
        functions.setVisibility(View.GONE);
        mDiscLayout = (RelativeLayout) findViewById(R.id.discLay);

        mScanButton = (Button) findViewById(R.id.scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int hasPermission = ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                    mSmoothBluetooth.doDiscovery();
                    return;
                }

                ActivityCompat.requestPermissions(BluetoothActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        25);
            }

        });


        databtn = (Button) findViewById(R.id.data);
        databtn.setOnClickListener(v -> {
            try {
                Intent intent2 = new Intent(BluetoothActivity.this, Data.class);
                startActivity(intent2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uploadData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mPairedButton = (Button) findViewById(R.id.paired);
        mPairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSmoothBluetooth.tryConnection();
            }
        });
        mStateTv = (TextView) findViewById(R.id.state);
        mStateTv.setText("Disconnected");
        mDeviceTv = (TextView) findViewById(R.id.device);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }


    public void writefile(String text, String filename) {
        try {

            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(filename + ".log", MODE_APPEND));
            String fixed = text.replaceAll("[^\\u0000-\\uFFFF]", "");
            fixed = fixed.replaceAll("[^\\p{ASCII}]", "");
            fixed = fixed.replaceAll("�", "");
            out.write(fixed);
            Log.d("fixed", fixed);
            out.close();

        } catch (Throwable t) {
            Toast.makeText(BluetoothActivity.this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void uploadData() throws IOException {
        Context context = this;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainActivityilunch");
            wakeLock.acquire();

            FileInputStream is;
            BufferedReader reader;
            List<File> files;
            PackageManager m = getPackageManager();
            String s = getPackageName();
            PackageInfo p = null;
            try {
                p = m.getPackageInfo(s, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            s = p.applicationInfo.dataDir;

            files = getListFiles(new File(s + "/files"));

            if (!files.isEmpty()) {
                for (File file : files) {
                    StringBuilder total = new StringBuilder();
                    Toast.makeText(BluetoothActivity.this, "Uploading " + file.toString(), Toast.LENGTH_SHORT).show();

                    if (file.exists()) {
                        is = new FileInputStream(file);
                        reader = new BufferedReader(new InputStreamReader(is));
                        String line = reader.readLine();
                        while (line != null) {
                            line = reader.readLine();
                            total.append(line + "|");
                            try {
                                Log.d("line", line.toString());
                                ratingsData obj = new ratingsData(line.split(",")[1], line.split(",")[0]);
                                ratingsDataarray.add(obj);
                            } catch (Exception e) {
                                Log.e("error", e.toString());

                            }

                        }
                        postData(total, file);

                    }
                }
            } else {
                Toast.makeText(BluetoothActivity.this, "Δεν υπάρχουν δεδομένα.", Toast.LENGTH_SHORT).show();

            }
            wakeLock.release();

        } else {
            Toast.makeText(BluetoothActivity.this, "No internet connection!", Toast.LENGTH_LONG).show();

        }
    }

    private void cpuwakelock(boolean sw) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainActivityilunch");
        if (sw == true) {
            wakeLock.acquire();
        } else {
            wakeLock.release();
        }
    }

    //Timer
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //linesrecieved >= totallines ||
            if (success==true) {
                Toast.makeText(BluetoothActivity.this, "Transfer successful!", Toast.LENGTH_LONG).show();
                //totallines = 0;
                //linesrecieved = 0;
                //mSmoothBluetooth.send("received", mCRLFBox.isChecked());
                handler.removeCallbacks(runnable);
                cpuwakelock(true);


            } else {
                handler.postDelayed(this, 100);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSmoothBluetooth.stop();
        try {
            handler.removeCallbacks(runnable);
        } catch (Throwable t) {
            Log.d("Exit", "Error in handler");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
     /*   switch (item.getItemId()) {
           case R.id.action_settings:

                return true;
            case R.id.action_data:
                Intent intent2 = new Intent(this, Data.class);
                startActivityForResult(intent2, 0);
                return true;
            case R.id.action_exit:
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }*/
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Θέλετε σίγουρα να αποχωρήσετε;")
                .setPositiveButton("ΝΑΙ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("ΟΧΙ", null)
                .show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT__REQUEST) {
            if (resultCode == RESULT_OK) {
                mSmoothBluetooth.tryConnection();
            }
        }
    }

    public void postData(StringBuilder filedata, File uploadedfile) {


        Gson gson = new Gson();
        String json = gson.toJson(ratingsDataarray);

        final String postdata = filedata.toString();
        final File uploadedfilefinal = uploadedfile;
        String str = uploadedfilefinal.toString();

        int endIndex = str.lastIndexOf("/");
        String filedataname = str.substring(endIndex + 1);
        endIndex = filedataname.lastIndexOf(".");
        filedataname = filedataname.substring(0, endIndex);
        final String finalfilename = filedataname;


        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://zafora.ece.uowm.gr/~ictest01041/ilunch_v10/public/api/submitFeedback?apiKey=s@r";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // response
                    Log.d("Response", response);
                    mResponseBuffer.add(0, "Success! " + response);
                    mResponsesAdapter.notifyDataSetChanged();
                    uploadedfilefinal.delete();
                    Toast.makeText(BluetoothActivity.this, "Success! " + response, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // error
                    Log.e("Error.Response", error.toString());
                    Log.e("Error.Response", new String(error.networkResponse.data).split("<h3 class=\"trace-class\">")[1]);

                    mResponseBuffer.add(0, "Error uploading data for " + finalfilename + ". " + error.toString());
                    mResponsesAdapter.notifyDataSetChanged();
                    Toast.makeText(BluetoothActivity.this, "Error uploading data for " + finalfilename + ". " + error.toString(), Toast.LENGTH_SHORT).show();

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data", json);

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(7500,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }


}
