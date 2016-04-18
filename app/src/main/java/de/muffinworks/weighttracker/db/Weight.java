package de.muffinworks.weighttracker.db;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by Bianca on 26.02.2016.
 */
public class Weight implements Comparable<Weight> {

    private static final double KILOS_TO_POUNDS = 2.20462;
    private static final double POUNDS_TO_KILOS = 0.453592;
    private static Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private int date;
    private double kilos;
    private double pounds;

    public Weight(int date, double kilos, double pounds) {
        this.date = date;
        this.kilos = kilos;
        this.pounds = pounds;
    }

    public Weight(int year, int month, int day, double kilos) {
        setDate(year, month - 1, day);
        setKilos(kilos);
    }

    public Weight(Date date) {
        setDate(date);
    }

    public Weight(Date date, double kilos) {
        setDate(date);
        setKilos(kilos);
    }

    public Date getDate() {
        return DateUtil.getDateFromInteger(date);
    }
    public int getDateInt() {
        return date;
    }

    public void setDate(int year, int month, int day) {
        c.set(year, month, day);
        this.date = DateUtil.getDateInteger(c.getTime());
    }

    public void setDate(Date date) {
        this.date = DateUtil.getDateInteger(date);
    }

    public long getDatetime() {
        c.setTime(DateUtil.getDateFromInteger(date));
        return c.getTimeInMillis();
    }

    public double getKilos() {
        return kilos;
    }

    public void setKilos(double kilos) {
        this.kilos = roundToOneDecimal(kilos);
        pounds = calcPounds(kilos);
    }

    public double getPounds() {
        return pounds;
    }

    public void setPounds(double pounds) {
        this.pounds = roundToOneDecimal(pounds);
        kilos = calcKilos(pounds);
    }

    public static double roundToOneDecimal(double num) {
        return Math.round(num * 10) / 10.0;
    }

    public static double calcPounds(double kilos) {
        return roundToOneDecimal(kilos * KILOS_TO_POUNDS);
    }

    public static double calcKilos(double pounds) {
        return roundToOneDecimal(pounds * POUNDS_TO_KILOS);
    }

    @Override
    public int compareTo(Weight another) {
        return getDateInt() - another.getDateInt();
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof Weight){
            Weight w = (Weight) o;
            retVal = DateUtil.getDateInteger(w.getDate()) == this.date;
        }

        return retVal;
    }

    public static double getAverage(List<Weight> weights) {
        double sum = 0;
        if(!weights.isEmpty()) {
            for (int i = 0, size = weights.size(); i < size; i++) {
                sum += weights.get(i).getKilos();
            }
            return sum / weights.size();
        }
        return sum;
    }
}
