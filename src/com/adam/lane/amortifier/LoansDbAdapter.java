package com.adam.lane.amortifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class LoansDbAdapter
{

    public static final String KEY_TITLE = "title";
    public static final String KEY_PRINCIPAL = "principal";
    public static final String KEY_RATE = "rate";
    public static final String KEY_TERM = "term";
    public static final String KEY_TERM_IN_MONTHS = "termMonths";
    public static final String KEY_EXTRA_MONTHLY_PAYMENT = "extra";

    public static final String KEY_ROWID = "_id";

    private static final String TAG = "LoansDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "loans";
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement, " + KEY_TITLE
            + " text not null DEFAULT '0', " + KEY_PRINCIPAL + " text not null DEFAULT '0', " + KEY_RATE + " text not null DEFAULT '0', " + KEY_TERM
            + " text not null DEFAULT '0', " + KEY_EXTRA_MONTHLY_PAYMENT + " text not null DEFAULT '0', " + KEY_TERM_IN_MONTHS
            + " integer no null DEFAULT '1');";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {

            if(oldVersion == 4 && newVersion == 5)
            {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_TERM_IN_MONTHS + " text not null DEFAULT '1'");
                //onCreate(db);
            }
            else
            {

                Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
                onCreate(db);
            }

        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx
     *            the Context within which to work
     */
    public LoansDbAdapter(Context ctx)
    {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException
     *             if the database could be neither opened or created
     */
    public LoansDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param Title
     *            the title of the note
     * @param Principal
     *            the body of the note
     * @return rowId or -1 if failed
     */
    public long createLoan(String Title, String Principal, String Rate, String Term, String ExtraMonthlyPayment)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, Title);
        initialValues.put(KEY_PRINCIPAL, Principal);
        initialValues.put(KEY_RATE, Rate);
        initialValues.put(KEY_TERM, Term);
        initialValues.put(KEY_EXTRA_MONTHLY_PAYMENT, ExtraMonthlyPayment);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId
     *            id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteLoan(long rowId)
    {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllLoans()
    {

        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_PRINCIPAL, KEY_RATE, KEY_TERM, KEY_TERM_IN_MONTHS,
                KEY_EXTRA_MONTHLY_PAYMENT }, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId
     *            id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException
     *             if note could not be found/retrieved
     */
    public Cursor fetchLoan(long rowId) throws SQLException
    {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_PRINCIPAL, KEY_RATE, KEY_TERM, KEY_TERM_IN_MONTHS,
                KEY_EXTRA_MONTHLY_PAYMENT }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if(mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId
     *            id of note to update
     * @param title
     *            value to set note title to
     * @param principal
     *            value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateLoan(long rowId, String principal, String rate, String term, Boolean termInMonths, String ExtraMonthlyPayment)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_PRINCIPAL, principal);
        args.put(KEY_RATE, rate);
        args.put(KEY_TERM, term);
        args.put(KEY_TERM_IN_MONTHS, (termInMonths ? 1 : 0));
        args.put(KEY_EXTRA_MONTHLY_PAYMENT, ExtraMonthlyPayment);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId
     *            id of note to update
     * @param title
     *            value to set note title to
     * @param principal
     *            value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean renameLoan(long rowId, String title)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
