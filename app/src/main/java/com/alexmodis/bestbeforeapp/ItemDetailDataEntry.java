package com.alexmodis.bestbeforeapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class ItemDetailDataEntry extends AppCompatActivity {
    private static final String LOG_TAG = ItemDetailDataEntry.class.getSimpleName();
    private EditText nameField;
    private EditText quantityField;
    private EditText expiryDateField;
    private EditText barcodeField;

    private ImageButton expiryDatescan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "onCreate" + true);
        setContentView(R.layout.item_data_entry);
        String barcode = "";
        barcode = getIntent().getStringExtra("barcode");

        nameField = this.findViewById(R.id.item_name);
        quantityField = this.findViewById(R.id.item_quantity);
        expiryDateField = this.findViewById(R.id.expiryDate);
        barcodeField = this.findViewById(R.id.item_barcode);

        barcodeField.setText(barcode);

        expiryDatescan = this.findViewById(R.id.expiryDatescan);

        expiryDatescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent barcodeScanner = new Intent(getApplicationContext(), com.alexmodis.bestbeforeapp.ExpiryDateScanner.ExpiryDateScannerActivity.class);
                startActivityForResult(barcodeScanner, 200);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

        }

        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                expiryDateField.setText(data.getStringExtra("expiryday_detected"));
            }

        }
    }

    public void saveNewItemButton(View view) {

        Context context = getApplicationContext();
        CharSequence toastText;
        int duration = Toast.LENGTH_SHORT;


        String name = null;
        Integer quantity = null;
        Date expiryDate = null;
        String barcode = null;


        if (nameField != null) {
            name = nameField.getText().toString();
        }
        if (barcodeField != null) {
            barcode = barcodeField.getText().toString();
        }
        if (quantityField != null) {
            String quantityString = quantityField.getText().toString();
            if (!quantityString.isEmpty()) {
                quantity = Integer.parseInt(quantityString);
            }
        }
        if (expiryDateField != null) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            try {
                expiryDate = df.parse(expiryDateField.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (name == null || name.isEmpty()) {
            toastText = "Name is empty";
            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();
        } else if (quantity == null) {
            toastText = "Quantity is empty";
            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();
        } else if (expiryDate == null) {
            toastText = "Expiry Date is empty or invalid";
            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();

        } else if (barcode == null) {
            toastText = "Barcode is empty";
            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();

        } else {

            Item newItem = new Item();
            Realm.init(this);
            RealmConfiguration realmConfig = new RealmConfiguration.
                    Builder().
                    deleteRealmIfMigrationNeeded().
                    build();
            Realm.setDefaultConfiguration(realmConfig);
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            final RealmResults<Item> items = realm.where(Item.class).findAll();
            int size = items.size();
            // Increment index
            int nextID = (size + 1);

            newItem.newItem(nextID, name, quantity, expiryDate, barcode);
            realm.copyToRealmOrUpdate(newItem);
            realm.commitTransaction();

            toastText = "Product Saved!";
            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();

            finish();

        }

    }

    public void cancelNewItemButton(View view) {
        cancelNewItem();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancelNewItem();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cancelNewItem() {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Cancel the New Product?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        //close();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();

    }


}
