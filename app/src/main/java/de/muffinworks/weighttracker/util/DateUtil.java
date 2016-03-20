package de.muffinworks.weighttracker.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bianca on 18.03.2016.
 */
public class DateUtil {

    private static Calendar c = Calendar.getInstance();

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
        return Calendar.getInstance().getTime();
    }

    public static boolean compareMonth(Date d1, Date d2) {
        Calendar c1 = (Calendar)c.clone();
        c1.setTime(d1);
        Calendar c2 = (Calendar)c.clone();
        c2.setTime(d2);

        return  c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

    public static boolean compareYear(Date d1, Date d2) {
        Calendar c1 = (Calendar)c.clone();
        c1.setTime(d1);
        Calendar c2 = (Calendar)c.clone();
        c2.setTime(d2);

        return  c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }

    public static boolean compareDay(Date d1, Date d2) {
        Calendar c1 = (Calendar)c.clone();
        c1.setTime(d1);
        Calendar c2 = (Calendar)c.clone();
        c2.setTime(d2);

        return  c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfMonth(Date d) {
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH);
    }
}
