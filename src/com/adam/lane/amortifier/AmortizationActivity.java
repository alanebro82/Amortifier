package com.adam.lane.amortifier;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adam.lane.amortifier.Loan.AmortizationMonth;

public class AmortizationActivity extends ListActivity
{
    private static final int SCROLL_TO_ID = Menu.FIRST;

    private Loan mLoan;
    private Long mRowId;
    private LoansDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // open DB connection
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.amortization_table);
        setTitle(R.string.amort_schedule);

        // get rowId from bundle
        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(LoansDbAdapter.KEY_ROWID);

        if(mRowId == null)
        {
            Bundle extras = getIntent().getExtras();
            mRowId = ((extras != null) ? extras.getLong(LoansDbAdapter.KEY_ROWID) : null);
            mRowId = 0 == mRowId ? null : mRowId;
        }

        // set title string
        Cursor note = mDbHelper.fetchLoan(mRowId);
        startManagingCursor(note);
        String name = note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_TITLE));
        TextView loanLabel = (TextView) findViewById(R.id.loanNameLabel);
        loanLabel.setText(name);

        populateFields();
    }

    private void populateFields()
    {
        try
        {
            if(mRowId != null)
            {
                Cursor note = mDbHelper.fetchLoan(mRowId);
                startManagingCursor(note);

                String extraString = note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_EXTRA_MONTHLY_PAYMENT));
                if(extraString.length() == 0)
                {
                    extraString = "0";
                }

                // get loan from database
                Double principal = Double.parseDouble(note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_PRINCIPAL)));
                Double rate = Double.parseDouble(note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_RATE))) / 100;
                Double term = Double.parseDouble(note.getString(note.getColumnIndexOrThrow(LoansDbAdapter.KEY_TERM)));
                Double extra = Double.parseDouble(extraString);

                // create loan
                mLoan = new Loan(principal, rate, term, extra);

                // build list view
                ArrayList<AmortizationMonth> amortTable = mLoan.getAmortizationTable();
                ListView listview = getListView();
                listview.setAdapter(new AmortizationTableBaseAdapter(this, amortTable));

                // build header strings
                TextView totalPaidLabel = (TextView) findViewById(R.id.totalPaidLabel);
                TextView totalInterestPaidLabel = (TextView) findViewById(R.id.totalInterestPaidLabel);
                TextView loanAmountLabel = (TextView) findViewById(R.id.loanAmountLabel);

                double total = 0;
                double interest = 0;

                for(AmortizationMonth amortizationMonth : amortTable)
                {
                    total += amortizationMonth.getTotalPaid();
                    interest += amortizationMonth.getInterestPaid();
                }

                // format string
                Locale currentLocale = Locale.getDefault();
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
                totalPaidLabel.setText(currencyFormatter.format(total));
                totalInterestPaidLabel.setText(currencyFormatter.format(interest));
                loanAmountLabel.setText(currencyFormatter.format(mLoan.getPrincipal()));
            }
        }
        catch(NumberFormatException e)
        {
            // create the toast
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, R.string.invalid_loan, duration).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState)
    {
        super.onRestoreInstanceState(inState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SCROLL_TO_ID, 0, R.string.menu_scroll_to);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case SCROLL_TO_ID:
                askScrollToPayment();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void askScrollToPayment()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.jump);
        alert.setMessage(R.string.enter_jump_number);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                try
                {
                    String value = input.getText().toString();
                    if(value.length() != 0)
                    {
                        scrollToPayment(Integer.parseInt(value));
                    }
                }
                catch(NumberFormatException e)
                {
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

    private void scrollToPayment(int position)
    {
        ListView listview = getListView();
        listview.setSelection(position - 1);
    }
}
