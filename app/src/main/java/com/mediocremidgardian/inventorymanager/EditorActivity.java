package com.mediocremidgardian.inventorymanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mediocremidgardian.inventorymanager.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;

/**
 * Created by Valhalla on 9/27/16.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {

    EditText mName;
    EditText mQuantity;
    EditText mPrice;
    ImageView mPicture;
    Uri mCurrentItemUri;

    //used to store thumbnail for db
    byte [] mByteArray;

    boolean mItemChanged = false;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemChanged = true;
            return false;
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPicture.setImageBitmap(imageBitmap);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            mByteArray = outputStream.toByteArray();
            }
        }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        Button addQuantity = (Button) findViewById(R.id.add_quantity);
        Button lowerQuantity = (Button) findViewById(R.id.subtract_quantity);
        TextView adjustQuantity = (TextView) findViewById(R.id.adjust_text_view);
        mPicture = (ImageView)findViewById(R.id.picture_view);

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.create_title));
//            hide views that aren't necessary when creating item
            adjustQuantity.setVisibility(View.GONE);
            addQuantity.setVisibility(View.GONE);
            lowerQuantity.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_title));

            //create CursorLoader to populate fields
            getLoaderManager().initLoader(0, null, this);
        }

        //initialize and connect touch listeners for edit texts
        mName = (EditText) findViewById(R.id.description_edit_text);
        mName.setOnTouchListener(mTouchListener);
        mQuantity = (EditText) findViewById(R.id.quantity_edit_text);
        mQuantity.setOnTouchListener(mTouchListener);
        mPrice = (EditText) findViewById(R.id.price_edit_text);
        mPrice.setOnTouchListener(mTouchListener);

        mPicture.setOnClickListener(mClickListener);

        addQuantity.setOnClickListener(this);
        lowerQuantity.setOnClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //hide menu options we don't need if we are in crete item mode
        if (mCurrentItemUri == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            MenuItem order = menu.findItem(R.id.action_order_more);
            delete.setVisible(false);
            order.setVisible(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveItem();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_order_more:
                orderMore();
                return true;

            case android.R.id.home:

                if (!mItemChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // There are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;


            default:
                return true;
        }

    }

    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_title);

        builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteItem();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog
            (DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void saveItem() {

        String name = mName.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();

        //if nothing was entered then we should not create new entry and return to list activity
        if (!mItemChanged) {
            return;
        } else if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || mByteArray == null) {
            Toast.makeText(this, "Please enter all information before trying to save Item", Toast.LENGTH_LONG).show();
            return;

        } else {

            ContentValues contentValues = new ContentValues();
            contentValues.put(ItemEntry.COLUMN_ITEM_NAME, name);

            int quantity = Integer.parseInt(quantityString);
            contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

            int price = Integer.parseInt(priceString);
            contentValues.put(ItemEntry.COLUMN_ITEM_PRICE, price);

            contentValues.put(ItemEntry.COLUMN_ITEM_PICTURE, mByteArray);

            if (mCurrentItemUri == null) {
                Uri uri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);

                if (uri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, R.string.add_error,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.add_success,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int updated = getContentResolver().update(mCurrentItemUri, contentValues, null, null);

                if (updated == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, R.string.update_error,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, R.string.update_success,
                            Toast.LENGTH_SHORT).show();
                }

            }
            finish();
        }
    }


    private void deleteItem() {

        if (mCurrentItemUri == null) {
            return;
        }

        int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.delete_error,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_success,
                    Toast.LENGTH_SHORT).show();
        }
        finish();

    }

    private void orderMore() {

        String email = "buyer@moreinventory.com";
        String subject = "Ordering more " + mName.getText().toString().trim();
        String body = "I would like to order more of " + mName.getText().toString().trim();
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_choose)));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_PICTURE};
        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumn = cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumn = cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumn = cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PRICE);
            int picColumn = cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PICTURE);

            String name = cursor.getString(nameColumn);
            String quantity = Integer.toString(cursor.getInt(quantityColumn));
            String price = Integer.toString(cursor.getInt(priceColumn));
            mByteArray = cursor.getBlob(picColumn);

            mName.setText(name);
            mQuantity.setText(quantity);
            mPrice.setText(price);
            mPicture.setImageBitmap(BitmapFactory.decodeByteArray(mByteArray,0,mByteArray.length));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mName.setText("");
        mQuantity.setText("");
        mPrice.setText("");
        mPicture.setImageDrawable(getDrawable(R.drawable.ic_photo_camera_black_36dp));
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.add_quantity:
                addInventory();
                break;
            case R.id.subtract_quantity:
                subtractInventory();
                break;
        }
    }

    private void subtractInventory() {
        int currentQuantity = Integer.parseInt(mQuantity.getText().toString().trim());
        if (currentQuantity - 1 >= 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, (currentQuantity - 1));
            getContentResolver().update(mCurrentItemUri, contentValues, null, null);
        } else {
            Toast.makeText(getApplicationContext(), R.string.zero_error, Toast.LENGTH_LONG).show();
        }
    }

    private void addInventory() {

        int currentQuantity = Integer.parseInt(mQuantity.getText().toString().trim());
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, (currentQuantity + 1));
        getContentResolver().update(mCurrentItemUri, contentValues, null, null);
    }

    @Override
    public void onBackPressed() {

        if (!mItemChanged) {
            super.onBackPressed();
            return;
        }


        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
