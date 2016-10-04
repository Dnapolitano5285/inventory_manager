package com.mediocremidgardian.inventorymanager.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Valhalla on 9/26/16.
 */

public final class ItemContract {

    /**Private constructor so no one can instantiate*/
    private ItemContract() {}

    /** content authority matching manifest and parsed to create scheme for matcher*/
    public static final String CONTENT_AUTHORITY = "com.mediocremidgardian.inventorymanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns{

        public static final String TABLE_NAME = "items";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_PICTURE = "picture";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /** helper method to check for valid prices*/
        public static boolean validPrice(int price){
            return (price>0);
        }
        /** helper method to check for valid quantities*/
        public static boolean validQuantity(int quantity){
            return (quantity>=0);
        }

    }
}
