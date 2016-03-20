package de.muffinworks.weighttracker.db;

import android.provider.BaseColumns;

/**
 * Created by Bianca on 25.02.2016.
 */
public final class WeightContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public WeightContract() {}

    /* Inner class that defines the table contents */
    public static abstract class WeightEntry implements BaseColumns {
        public static final String TABLE_NAME = "weight";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_KILOS = "kilos";
        public static final String COLUMN_NAME_POUNDS = "pounds";
    }
}
