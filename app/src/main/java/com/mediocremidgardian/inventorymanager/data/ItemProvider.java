package com.mediocremidgardian.inventorymanager.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.mediocremidgardian.inventorymanager.data.ItemContract.ItemEntry;

/**
 * Content Provider for inventory app
 */

public class ItemProvider extends ContentProvider{

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private ItemDbHelper mDbHelper;

    /** URI matcher code for the content URI for the Item table*/
    private static final int ITEMS = 1000;

    /** URI matcher code for the content URI for the Item table*/
    private static final int ITEMS_ID= 1001;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEMS_ID);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    /** Query the given URI and return cursor which has the results*/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            //query full database
            case ITEMS:

                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            //query for a single item
            case ITEMS_ID:

                // replace selection with the id of the item we are searching for
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(ItemEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;

            default: throw new IllegalArgumentException("Unknown URI, cannot query " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMS:
                return insertItem(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion not available for " + uri);
        }
    }

    /** Insert a new Item into the database based on the contentvalues and uri passed into insert*/
    public Uri insertItem(Uri uri, ContentValues values){
        String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        int quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        int price = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);

        if (name == null){
            throw new IllegalArgumentException("Items require a name");
        } else if (!ItemEntry.validQuantity(quantity)){
            throw new IllegalArgumentException("Item must have quantity greater than 0");
        } else if (!ItemEntry.validPrice(price)){
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ItemEntry.TABLE_NAME,null,values);

        if(id == -1){
            Log.e(LOG_TAG, "Failed inserting for for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match){
            case ITEMS:
                //delete all rows
                return db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            case ITEMS_ID:
                //delete a single item
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete failed");
        }
    }

    /**Updated a given item or items with the provided contentvalues*/
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                //update information across the entire table
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEMS_ID:
                //update the information on a single item
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                return 0;
        }
    }


      public int updateItem(Uri uri, ContentValues contentValues, String selection, String [] selectionArgs) {
    /** The following if statements provide data validation for the values before inserting*/
        if(contentValues.size()==0){
            return 0;
        }

        if(contentValues.containsKey(ItemEntry.COLUMN_ITEM_NAME)){
            String name = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null){
                throw new IllegalArgumentException("Items require a name");
            }
        }

        if(contentValues.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)){
            Integer quantity = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if(quantity == null || !ItemEntry.validQuantity(quantity)){
                throw new IllegalArgumentException("Items require a valid quantity");
            }
        }

        if(contentValues.containsKey(ItemEntry.COLUMN_ITEM_PRICE)){
            Integer price = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
            if(price == null || !ItemEntry.validPrice(price)){
                throw new IllegalArgumentException("Item must have valid price");
            }
        }


        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri,null);
        return db.update(ItemEntry.TABLE_NAME,contentValues,selection,selectionArgs);
    }
}
