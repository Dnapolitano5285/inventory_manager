package com.mediocremidgardian.inventorymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mediocremidgardian.inventorymanager.data.ItemContract.ItemEntry;
/**
 * Database helper for Inventory app. Manages database creation and versions
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /** Name of Database file */
    private static final String DATABASE_NAME = "inventory.db";

    /** database version */
    private static final int DATABASE_VERSION = 1;

    /** constructor for an instance of dbHelper,
    @param context is of the app*/
    public ItemDbHelper(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create a String which is the SQL statement to create the items table
        String SQL_CREATE_ITEM_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PICTURE + " BLOB" + ");";

        //create db
        db.execSQL(SQL_CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //nothing needed here at this time as we are not upgrading db
    }
}
