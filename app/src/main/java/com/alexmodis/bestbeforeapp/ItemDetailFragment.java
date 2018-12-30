package com.alexmodis.bestbeforeapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.NumberFormat;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class ItemDetailFragment extends Fragment {

    private static final String LOG_TAG = ItemListAdapter.class.getSimpleName();
    public static final String ARG_ITEM_ID = "item_id";
    private View rootView;
    private int itemId;
    private String itemexpiryDate;
    private String itemQtyString;


    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        itemId = intent.getIntExtra("item_id", 0);
        Realm.init(getContext());
        RealmConfiguration realmConfig = new RealmConfiguration.
                Builder().
                deleteRealmIfMigrationNeeded().
                build();
        Realm.setDefaultConfiguration(realmConfig);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final RealmResults<Item> items = realm.where(Item.class).equalTo("id", itemId).findAll();
        realm.commitTransaction();


        Item item = items.get(0);
        //NumberFormat defaultFormat = NumberFormat.getCurrencyInstance();
        itemexpiryDate = item.getExpiryDate().toString();
        int itemQty = item.getQuantity();
        itemQtyString = Integer.toString(itemQty);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(com.alexmodis.bestbeforeapp.R.layout.item_detail, container, false);

        TextView itemexpiryDateView = rootView.findViewById(R.id.item_detail_price);
        TextView itemQtyView = rootView.findViewById(R.id.item_detail_qty);


        FloatingActionButton editItemButton = getActivity().findViewById(R.id.item_edit);

        Button itemQuantityAddButton = rootView.findViewById(R.id.item_quantity_add);
        Button itemQuantityRemoveButton = rootView.findViewById(R.id.item_quantity_remove);

        Button itemDeleteButton = rootView.findViewById(R.id.item_detail_delete);

        itemexpiryDateView.setText(itemexpiryDate);
        itemQtyView.setText(itemQtyString);

        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editItem(itemId);
            }
        });
        itemQuantityAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityAdd(itemId, 1);
            }
        });
        itemQuantityRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityRemove(itemId, 1);
            }
        });

        itemDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemDelete(itemId);
            }
        });

        return rootView;
    }


    private void quantityAdd(int itemId, int itemQty) {
        modifyQuantityOnHand("add", itemId, itemQty);
    }

    private void quantityRemove(int itemId, int itemQty) {
        modifyQuantityOnHand("remove", itemId, itemQty);
    }

    private void modifyQuantityOnHand(String action, int itemId, int itemQty) {


        Realm.init(getContext());
        RealmConfiguration realmConfig = new RealmConfiguration.
                Builder().
                deleteRealmIfMigrationNeeded().
                build();
        Realm.setDefaultConfiguration(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final RealmResults<Item> items = realm.where(Item.class).equalTo("id", itemId).findAll();

        Item item = items.get(0);

        int newQty;

        switch (action) {
            case "add":
                newQty = item.getQuantity() + itemQty;
                item.setQuantity(newQty);
                realm.commitTransaction();
                updateQuantityView(Integer.toString(newQty));
                break;
            case "remove":
                newQty = item.getQuantity() - itemQty;
                if (newQty <= 0) {
                    newQty = 0;
                }
                item.setQuantity(newQty);
                realm.commitTransaction();
                updateQuantityView(Integer.toString(newQty));
                break;
            default:
                realm.commitTransaction();
                break;

        }

    }


    private void updateQuantityView(String newQty) {
        if (rootView == null) {
            return;
        }
        TextView itemQtyView = rootView.findViewById(R.id.item_detail_qty);
        itemQtyView.setText(newQty);
    }


    private void editItem(int itemId) {

    }



    private void itemDelete(final int itemId) {
        AlertDialog alertbox = new AlertDialog.Builder(getActivity())
                .setMessage("Delete product?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        Realm.init(getContext());
                        RealmConfiguration realmConfig = new RealmConfiguration.
                                Builder().
                                deleteRealmIfMigrationNeeded().
                                build();
                        Realm.setDefaultConfiguration(realmConfig);
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        final RealmResults<Item> items = realm.where(Item.class).equalTo("id", itemId).findAll();
                        Item item = items.get(0);
                        item.deleteFromRealm();
                        realm.commitTransaction();
                        getActivity().finish();
                        //close();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();

    }
}
