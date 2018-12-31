package com.alexmodis.bestbeforeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Intent intent = getIntent();
        int alarm_code = intent.getIntExtra("requestCode", 0);
        String product = intent.getStringExtra("product");
        String expiryDate = intent.getStringExtra("expiryDate");
    }
}
