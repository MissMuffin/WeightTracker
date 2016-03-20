package de.muffinworks.weighttracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.muffinworks.weighttracker.db.WeightContract.WeightEntry;
import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by Bianca on 25.02.2016.
 */
public class WeightDbService {

    private SQLiteDatabase db;
    private WeightDbHelper dbHelper;
    private String[] allDbColumns = {
            WeightEntry.COLUMN_NAME_DATE,
            WeightEntry.COLUMN_NAME_KILOS,
            WeightEntry.COLUMN_NAME_POUNDS
    };

    public WeightDbService(Context context) {
        dbHelper = new WeightDbHelper(context);
        open();
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void putWeightEntry(Weight weight) {
        ContentValues values = new ContentValues();
        values.put(WeightEntry.COLUMN_NAME_DATE, weight.getDateInt());
        values.put(WeightEntry.COLUMN_NAME_KILOS, weight.getKilos());
        values.put(WeightEntry.COLUMN_NAME_POUNDS, weight.getPounds());

        if(doesEntryExist(weight.getDateInt())) {
            Log.w("DB SERVICE ERROR", "entry for "+weight.getDateInt()+" already exists");
            db.update(WeightEntry.TABLE_NAME, values, WeightEntry.COLUMN_NAME_DATE + "=" + weight.getDateInt(), null);
        }else{
            db.insert(WeightEntry.TABLE_NAME, null, values);
        }
    }

    private Weight cursorToWeight(Cursor c) {
        return new Weight(
                c.getInt(c.getColumnIndexOrThrow(WeightEntry.COLUMN_NAME_DATE)),
                c.getDouble(c.getColumnIndexOrThrow(WeightEntry.COLUMN_NAME_KILOS)),
                c.getDouble(c.getColumnIndexOrThrow(WeightEntry.COLUMN_NAME_POUNDS))
        );
    }

    private boolean doesEntryExist(int datetime) {
        String Query = "Select * from "
                + WeightEntry.TABLE_NAME
                + " where "
                + WeightEntry.COLUMN_NAME_DATE
                + " = " + datetime;
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public Weight get(Date date) {
        Cursor cursor = db.query(
                WeightEntry.TABLE_NAME,
                allDbColumns,
                WeightEntry.COLUMN_NAME_DATE+"="+ DateUtil.getDateInteger(date),
                null,
                null,
                null,
                null);
        int count = cursor.getCount();
        if (count>1) {
            Log.w("DB SERVICE WARNING", "get query for date returned more than one row");
            return null;
        }else if(count<1) {
            Log.w("DB SERVICE WARNING", "get query for date did not return anything");
            return null;
        }
        cursor.moveToFirst();
        Weight w = cursorToWeight(cursor);
        cursor.close();
        return w;
    }

    public List<Weight> get(Date fromDate, Date toDate) {
        Cursor cursor = db.query(
                WeightEntry.TABLE_NAME,
                allDbColumns,
                WeightEntry.COLUMN_NAME_DATE
                        + " BETWEEN "
                        + DateUtil.getDateInteger(fromDate)
                        +" AND "
                        +DateUtil.getDateInteger(toDate),
                null,
                null,
                null,
                null);
        if(cursor.getCount()<1) {
            Log.w("DB SERVICE WARNING", "get query for date did not return anything");
            return null;
        }
        List<Weight> entries = new ArrayList<Weight>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Weight w = cursorToWeight(cursor);
            entries.add(w);
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    public List<Weight> getCurrentWeek() {
        //get cal obj with current date
        //get first day of week from cal and set
        //add 7

        return null;
    }

    public List<Weight> getCurrentMonth() {
        //select for current month in time double
        return null;
    }

    public List<Weight> getCurrentYear() {
        //select for current year in time double
        return null;
    }

    /*private List<Weight> cursorToList(Cursor cursor) {
        List<Weight> toReturn = new ArrayList<>();
        while(cursor.moveToNext()) {
            toReturn.add(cursorToWeight(cursor));
        }
        return toReturn;
    }

    public List<Weight> getAll() {
        Cursor cursor = db.rawQuery("SELECT * FROM weight ORDER BY date", new String[] {});
        return cursorToList(cursor);
    }*/

    public void deleteEntry(Date date) {
        int rowsAffected = db.delete(
                WeightEntry.TABLE_NAME,
                WeightEntry.COLUMN_NAME_DATE + "=" + DateUtil.getDateInteger(date),
                null);
        if (rowsAffected>1) Log.w("DB SERVICE DELETE", "more than on row affected by delete");
    }


    public List<Weight> getAllEntries() {
        List<Weight> entries = new ArrayList<Weight>();
        Cursor cursor = db.query(WeightEntry.TABLE_NAME, allDbColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Weight w = cursorToWeight(cursor);
            entries.add(w);
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    public void createDummyEntries() {
        Calendar c = Calendar.getInstance();
        Random random = new Random();
        double val = 65;
        for(int i = 0; i < 365; ++i) {
            c.add(Calendar.DATE, -1);
            putWeightEntry(new Weight(c.getTime(), val));
            val += (random.nextDouble() -  0.5) * 1;
        }
    }
}
