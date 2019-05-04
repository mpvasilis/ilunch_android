package com.vasilis.ilunch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.google.firebase.FirebaseApp;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ItemListActivity extends AppCompatActivity {
    private static final String LOG_TAG = ItemListActivity.class.getSimpleName();
    private PendingIntent pendingIntent;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Intent intent = new Intent(getApplicationContext(), ItemDetailDataEntry.class);
                    //startActivity(intent);
                    scanBarcode();
                }
            });
        }
        FirebaseApp.initializeApp(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void scanBarcode() {
        Intent barcodeScanner = new Intent(this, com.vasilis.ilunch.BarcodeScanner.BarcodeScannerActivity.class);
        startActivity(barcodeScanner);
    }

    /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
            case R.id.add_product:
                Intent intent = new Intent(getApplicationContext(), ItemDetailDataEntry.class);
                startActivity(intent);
                return true;
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.exit:
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/



    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.
                Builder().
                deleteRealmIfMigrationNeeded().
                build();
        Realm.setDefaultConfiguration(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int short_option = Integer.parseInt(sharedPref.getString("productsortoptions", "0"));
        String sortfield;
        Log.e(LOG_TAG, "short " + short_option);
        final RealmResults<Item> items = realm.where(Item.class).findAll();
        switch (short_option) {
            case 0:
                sortfield = "name";
                break;
            case 1:
                sortfield = "expiryDate";
                break;
            case 2:
                sortfield = "quantity";
                break;
            default:
                sortfield = "name";
                break;
        }
        realm.commitTransaction();
        int size = items.size();
        Log.e(LOG_TAG, "size " + size);
        if (size > 0) {
            recyclerView.setAdapter(new ItemListAdapter(this, realm.where(Item.class).findAllAsync().sort(sortfield)));
            recyclerView.setHasFixedSize(true);
        }

    }





}
