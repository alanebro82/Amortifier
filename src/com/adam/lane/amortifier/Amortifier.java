package com.adam.lane.amortifier;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Amortifier extends ListActivity
{
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int ACTIVITY_AMORTIZATION = 2;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int RENAME_ID = Menu.FIRST + 3;

    private LoansDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loans_list);
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();

        fillData();

        registerForContextMenu(getListView());
    }

    private void fillData()
    {
        Cursor notesCursor = mDbHelper.fetchAllLoans();
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list
        // (only TITLE)
        String[] from = new String[] { LoansDbAdapter.KEY_TITLE };

        // and an array of the fields we want to bind those fields to (in this
        // case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.loans_row, notesCursor, from, to);
        setListAdapter(notes);

        // create add button and add click listener
        Button addButton = (Button) findViewById(R.id.AddButton);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showNewLoanDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case INSERT_ID:
                showNewLoanDialog();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, RENAME_ID, 0, R.string.menu_rename);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId())
        {
            case DELETE_ID:
                mDbHelper.deleteLoan(info.id);
                fillData();
                return true;
            case EDIT_ID:
                Intent i = new Intent(this, LoanEdit.class);
                i.putExtra(LoansDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
            case RENAME_ID:
                showRenameLoanDialog(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createLoan(String title)
    {
        Intent i = new Intent(this, LoanEdit.class);
        i.putExtra(getString(R.string.title), title);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    private void showNewLoanDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.new_loan);
        alert.setMessage(R.string.enter_new_loan_name);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String value = input.getText().toString();
                if(value.length() != 0)
                {
                    createLoan(value);
                }
                else
                {
                    // create the toast
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, R.string.no_blank_names, duration).show();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Canceled.
                return;
            }
        });

        alert.show();
    }

    private void showRenameLoanDialog(final Long dbRow)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.rename_loan);
        alert.setMessage(R.string.enter_new_loan_name);

        final Cursor note = mDbHelper.fetchLoan(dbRow);
        startManagingCursor(note);
        String name = note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_TITLE));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(name);
        input.selectAll();
        input.requestFocus();
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String value = input.getText().toString();
                if(value.length() != 0)
                {
                    mDbHelper.renameLoan(dbRow, value);
                    fillData();
                }
                else
                {
                    // create the toast
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, R.string.no_blank_names, duration).show();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Canceled.
                return;
            }
        });

        alert.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent i = new Intent(this, AmortizationActivity.class);
        i.putExtra(LoansDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_AMORTIZATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode)
        {
            case ACTIVITY_CREATE:// creating new loan
                switch(resultCode)
                {
                    case RESULT_OK:// new loan created, update list
                        fillData();
                        break;
                    case RESULT_CANCELED:// nothing changed, ignore
                        break;
                }
                break;
            case ACTIVITY_EDIT:// editing loan
                switch(resultCode)
                {
                    case RESULT_OK:// loan edited, update list
                        break;
                    case RESULT_CANCELED:// nothing changed, ignore
                        break;
                }
                break;
        }
    }
}
