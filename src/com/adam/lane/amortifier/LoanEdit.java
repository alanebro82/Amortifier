package com.adam.lane.amortifier;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class LoanEdit extends Activity
{
    private String mTitle;

    private EditText mPrincipalText;
    private EditText mRateText;
    private EditText mTermText;
    private EditText mExtraPaymentText;
    
    private Spinner mMonthSpinner;

    private TextView mMinimumMonthlyPaymentLabel;
    private TextView mExtraMonthlyPaymentLabel;
    private TextView mTotalMonthlyPaymentLabel;

    private Long mRowId;
    private LoansDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.loan_edit);
        setTitle(R.string.edit_loan);

        mPrincipalText = (EditText) findViewById(R.id.principalEditText);
        mRateText = (EditText) findViewById(R.id.rateEditText);
        mTermText = (EditText) findViewById(R.id.termEditText);
        mExtraPaymentText = (EditText) findViewById(R.id.extraMonthlyPaymentEditText);
        
        mMonthSpinner = (Spinner)findViewById(R.id.monthSpinner);

        mMinimumMonthlyPaymentLabel = (TextView) findViewById(R.id.minimumMonthlyLabel);
        mExtraMonthlyPaymentLabel = (TextView) findViewById(R.id.extraMonthlyLabel);
        mTotalMonthlyPaymentLabel = (TextView) findViewById(R.id.totalMonthlyLabel);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.monthSpinnerOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(adapter);


        // get rowId from bundle
        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(LoansDbAdapter.KEY_ROWID);

        if(mRowId == null)
        {
            Bundle extras = getIntent().getExtras();
            mRowId = ((extras != null) ? extras.getLong(LoansDbAdapter.KEY_ROWID) : null);
            mRowId = 0 == mRowId ? null : mRowId;
        }

        // get title from bundle
        mTitle = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable("Title");

        if(mTitle == null)
        {
            Bundle extras = getIntent().getExtras();
            mTitle = ((extras != null) ? extras.getString(getString(R.string.title)) : null);
        }

        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                saveState();
                setResult(RESULT_OK);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // add filters
        mPrincipalText.setFilters(new InputFilter[] { new MoneyInputFilter() });
        mRateText.setFilters(new InputFilter[] { new RateInputFilter() });
        mExtraPaymentText.setFilters(new InputFilter[] { new MoneyInputFilter() });

        // add on-changed listeners
        mPrincipalText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                getMonthlyPayment();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
        mRateText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                getMonthlyPayment();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
        mTermText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                getMonthlyPayment();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
        mExtraPaymentText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                getMonthlyPayment();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        // fire the monthly payment function
        getMonthlyPayment();
    }

    private void populateFields()
    {
        if(mRowId != null)
        {
            Cursor loan = mDbHelper.fetchLoan(mRowId);
            startManagingCursor(loan);
            mPrincipalText.setText(loan.getString(loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_PRINCIPAL)));
            mRateText.setText(loan.getString(loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_RATE)));
            mTermText.setText(loan.getString(loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_TERM)));
            mExtraPaymentText.setText(loan.getString(loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_EXTRA_MONTHLY_PAYMENT)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(LoansDbAdapter.KEY_ROWID, mRowId);
        outState.putSerializable(getString(R.string.title), mTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState)
    {
        super.onRestoreInstanceState(inState);
        mRowId = (Long) inState.getSerializable(LoansDbAdapter.KEY_ROWID);
        mTitle = (String) inState.getSerializable(getString(R.string.title));
    }

    private void saveState()
    {
        String title = mTitle;
        String principal = mPrincipalText.getText().toString();
        String rate = mRateText.getText().toString();
        String term = mTermText.getText().toString();
        String extra = mExtraPaymentText.getText().toString();

        if(mRowId == null)
        {
            long id = mDbHelper.createLoan(title, principal, rate, term, extra);
            if(id > 0)
            {
                mRowId = id;
            }
        }
        else
        {
            //TODO: update the true
            mDbHelper.updateLoan(mRowId, principal, rate, term, true, extra);
        }
    }

    private void getMonthlyPayment()
    {
        try
        {
            // get data from widgets, initialize to 0 or 1 if empty
            String principalStr = mPrincipalText.getText().toString();
            principalStr = (principalStr.length() == 0) ? "0" : principalStr;
            String rateStr = mRateText.getText().toString();
            rateStr = (rateStr.length() == 0) ? "0" : rateStr;
            String termStr = mTermText.getText().toString();
            termStr = (termStr.length() == 0) ? "1" : termStr;
            String extraPaymentStr = mExtraPaymentText.getText().toString();
            extraPaymentStr = (extraPaymentStr.length() == 0) ? "0" : extraPaymentStr;

            // convert to number
            Double principal = Double.parseDouble(principalStr);
            Double rate = Double.parseDouble(rateStr) / 100;
            Double term = Double.parseDouble(termStr);
            Double extra = Double.parseDouble(extraPaymentStr);

            // create loan object and get monthly payment
            Loan loan = new Loan(principal, rate, term);
            Double monthlyPayment = loan.getMonthlyPayment();

            // create string formatter
            Locale currentLocale = Locale.getDefault();
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);

            // fill labels
            mMinimumMonthlyPaymentLabel.setText(currencyFormatter.format(monthlyPayment));
            mExtraMonthlyPaymentLabel.setText(currencyFormatter.format(extra));
            mTotalMonthlyPaymentLabel.setText(currencyFormatter.format(monthlyPayment + extra));

        }
        catch(NumberFormatException e)
        {
            // no need to catch NumberFormatExceptions... just ignore them
        }
    }
}
