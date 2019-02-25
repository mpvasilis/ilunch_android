package com.vasilis.ilunch;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


public class ItemListAdapter extends RealmRecyclerViewAdapter<Item, ItemListAdapter.MyViewHolder> {
    private static final String LOG_TAG = ItemListAdapter.class.getSimpleName();
    private final ItemListActivity activity;

    public ItemListAdapter(ItemListActivity activity, OrderedRealmCollection<Item> data) {
        super(activity, data, true);
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_list_content, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Item item = getData().get(position);
        holder.data = item;

        // Get the data from the Item object

        String item_name = item.getName();

        int item_id_int = item.getId();
        String item_id = Integer.toString(item_id_int);

        int item_qty_int = item.getQuantity();
        String item_qty = Integer.toString(item_qty_int);


        Date expiryDate = item.getExpiryDate();
        String item_expiryDate;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        item_expiryDate = df.format(expiryDate);


        holder.item_id.setText(item_id);
        holder.item_name.setText(item_name);
        holder.item_qty.setText(item_qty);
        holder.item_expiry.setText(item_expiryDate);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout item_row;
        public final TextView item_id;
        public final TextView item_name;
        public final TextView item_qty;
        public final TextView item_expiry;
        public Item data;

        public MyViewHolder(View view) {
            super(view);
            item_row = view.findViewById(R.id.item_row);
            item_id = view.findViewById(R.id.item_id);
            item_name = view.findViewById(R.id.item_name);
            item_qty = view.findViewById(R.id.item_qty);
            item_expiry = view.findViewById(R.id.item_price);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(activity.getApplicationContext(), ItemDetailActivity.class);
                    intent.putExtra("item_id", Integer.parseInt(item_id.getText().toString()));
                    intent.putExtra("item_name", item_name.getText());
                    activity.startActivity(intent);

                }
            });
        }

    }
}
