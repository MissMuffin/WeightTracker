package de.muffinworks.weighttracker.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Bianca on 18.03.2016.
 */
public class DateUtil {

    private static Calendar c = Calendar.getInstance(Locale.getDefault());
    private static final int[] CALENDAR_FIELDS = {
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH
        };

    public static Date getDateFromInteger(int date) {
        String s = date+"";
        int year = Integer.parseInt(s.substring(0, 4));
        int month = Integer.parseInt(s.substring(4, 6));
        int day = Integer.parseInt(s.substring(6, 8));
        c.set(year, month, day);
        return c.getTime();
    }

    public static int getDateInteger(Date date) {
        c.setTime(date);
        String year = c.get(Calendar.YEAR)+"";
        String month = String.format("%02d", c.get(Calendar.MONTH));
        String day = String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
        return Integer.parseInt("" + year + month + day);
    }

    public static Date currentDate() {
        return Calendar.getInstance(Locale.getDefault()).getTime();
    }

    public static boolean compareMonth(Date d1, Date d2) {
       return  compare(d1, d2, Calendar.MONTH);
    }

    public static boolean compareYear(Date d1, Date d2) {
       return  compare(d1, d2, Calendar.YEAR);
    }

    public static boolean compareDay(Date d1, Date d2) {
        return  compare(d1, d2, Calendar.DAY_OF_MONTH);
    }

    private static boolean compare(Date d1, Date d2, int field) {
        if (checkValidField(field)) {
            Calendar c1 = (Calendar)c.clone();
            c1.setTime(d1);
            Calendar c2 = (Calendar)c.clone();
            c2.setTime(d2);
            return c1.get(field) == c2.get(field);
        }
        return false;
    }

    public static boolean compare(Date d1, Date d2) {
        boolean year = compare(d1, d2, Calendar.YEAR);
        boolean month = compare(d1, d2, Calendar.MONTH);
        boolean day = compare(d1, d2, Calendar.DAY_OF_MONTH);
        return (
                compare(d1, d2, Calendar.YEAR)
                && compare(d1, d2, Calendar.MONTH)
                && compare(d1, d2, Calendar.DAY_OF_MONTH)
                );
    }

    private static boolean checkValidField(int field) {
        for (int val : CALENDAR_FIELDS) {
            if (field == val) return true;
        }
        return false;
    }

    public static int getDayOfMonth(Date d) {
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH);
    }
}
