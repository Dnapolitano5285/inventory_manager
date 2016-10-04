package com.mediocremidgardian.inventorymanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mediocremidgardian.inventorymanager.data.ItemContract.ItemEntry;

/**
 * Created by Valhalla on 9/27/16. BLORP
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView description = (TextView) view.findViewById(R.id.name);
        TextView quantity = (TextView) view.findViewById(R.id.quantity);
        TextView price = (TextView) view.findViewById(R.id.price);
        Button sale = (Button) view.findViewById(R.id.record_sale);

        sale.setTag(cursor.getPosition());

        String itemDescription = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_NAME));
        String itemQuantity = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY)));
        String itemPrice = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PRICE)));

        description.setText(itemDescription);

        quantity.setText(R.string.list_quantity);
        quantity.append(itemQuantity);

        price.setText("$");

        price.append(itemPrice);

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int position = (int)view.getTag();
                cursor.moveToPosition(position);
                
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry._ID));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY));
                int newQuantity = quantity - 1;

                if (newQuantity < 0) {
                    Toast.makeText(view.getContext(), R.string.reduce_error, Toast.LENGTH_SHORT).show();
                } else {
                    Uri currentItem = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                    context.getContentResolver().update(currentItem, contentValues, null, null);
                }
            }
        });
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }
}
