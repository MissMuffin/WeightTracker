package de.muffinworks.weighttracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.muffinworks.weighttracker.db.WeightContract.WeightEntry;

/**
 * Created by Bianca on 25.02.2016.
 */
public class WeightDbHelper extends SQLiteOpenHelper {

    public static final int DATABSE_VERSION = 3;
    public static final String DATABASE_NAME = "Weight.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WeightEntry.TABLE_NAME + " (" +
                    WeightEntry.COLUMN_NAME_DATE + " INTEGER primary key not null, " +
                    WeightEntry.COLUMN_NAME_KILOS + " NUMERIC, " +
                    WeightEntry.COLUMN_NAME_POUNDS + " NUMERIC" +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + WeightEntry.TABLE_NAME;

    public WeightDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(WeightDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
