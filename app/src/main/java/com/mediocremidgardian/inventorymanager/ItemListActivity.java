package com.mediocremidgardian.inventorymanager;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mediocremidgardian.inventorymanager.data.ItemContract.ItemEntry;

public class ItemListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Create necessary global variables
     */
    ItemCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        setTitle(getString(R.string.inventory_title));

        // Setup FAB to open item EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemListActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view_items);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        mCursorAdapter = new ItemCursorAdapter(this, null, 0);

        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ItemListActivity.this, EditorActivity.class);
                Uri currentItem = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                intent.setData(currentItem);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY, ItemEntry.COLUMN_ITEM_PRICE};

        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
