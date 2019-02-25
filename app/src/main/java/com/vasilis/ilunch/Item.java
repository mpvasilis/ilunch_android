package com.vasilis.ilunch;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class                Item extends RealmObject {
    private static final String LOG_TAG = Item.class.getSimpleName();

    @PrimaryKey
    private int id;
    private String name;
    private int quantity;
    private Date expiryDate;
    private String barcode;


    public void newItem(int id, String name, int quantity, Date expiryDate, String barcode) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.barcode = barcode;

    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
