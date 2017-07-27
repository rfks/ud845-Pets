/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.ItemEntry;

import static android.R.attr.id;

/**
 * Displays list of items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the item data loader */
    private static final int ITEM_LOADER = 0;

    /** Adapter for the ListView */
    ItemCursorAdapter mCursorAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the item data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);



        // Setup the item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ItemEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventory/item/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }
        });



        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);




    }

    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys,
        // and a laptop item attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "laptop");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, "supplier@laptopsupplier.ie");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 10);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 700);
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, "Not Available");

        // Insert a new row for laptop into the provider using the ContentResolver.
        // Use the {@link ItemEntry#CONTENT_URI} to indicate that we want to insert
        // into the item database table.
        // Receive the new content URI that will allow us to access laptop's data in the future.
        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
        ItemEntry.COLUMN_ITEM_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursorAdapter} with this new cursor containing updated item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


    public void onClickHandler(View v)
    {
        //TODO:  update the DB
        //v.getParent().getParent();

        /* can change the layout
        LinearLayout vwParentRow = (LinearLayout)v.getParent();

        TextView child = (TextView)vwParentRow.getChildAt(1);
        child.setText("overwrite");
        */

        /* but not the DB
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, String.valueOf(0));
        int id = ContentUris.parseId();
        Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
        int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
        */

        ListView lv = (ListView) v.getParent().getParent();
        int p=lv.getPositionForView(v);

        long i=mCursorAdapter.getItemId(p);

        Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, i);

        ContentValues values = new ContentValues();
     //   String[] select=(String[]) ItemEntry.COLUMN_ITEM_QUANTITY;
        Cursor cursor;
        cursor = getContentResolver().query(currentItemUri,new String[]{"quantity"},"where _id=?",new String[] {Long.toString(i)},null);

        //int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        //String picturePath = cursor.getString(columnIndex);

       //// Integer q = cursor.getInt(0);




        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, String.valueOf(p));
        int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
        Toast.makeText(this, "Sale button clicked!".concat(Integer.toString(p)), Toast.LENGTH_SHORT).show();

    }


}
