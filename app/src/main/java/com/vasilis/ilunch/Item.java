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
    private String cardtype;
    private String msg;


    public void newItem(int id, String name, String cardtype, String msg) {
        this.id = id;
        this.name = name;
        this.cardtype = cardtype;
        this.msg = msg;
        this.barcode = barcode;

    }
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

    public void setName(String name) {
        this.name = name;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
