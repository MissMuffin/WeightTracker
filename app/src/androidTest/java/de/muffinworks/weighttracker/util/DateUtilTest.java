package de.muffinworks.weighttracker.util;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Bianca on 19.03.2016.
 */
@SmallTest
public class DateUtilTest {

    @Test
    public void testCompareMonth() {
        //set up calendar obj
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);

        c.set(Calendar.MONTH, 1);
        Date jan = c.getTime();
        c.set(Calendar.MONTH, 2);
        Date feb = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);
        Date febPlus = c.getTime();

        assertThat(DateUtil.compareMonth(jan, feb), is(false));
        assertThat(DateUtil.compareMonth(feb, febPlus), is(true));
    }

    @Test
    public void testCompareYear() {
        //set up calendar obj
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, 1);

        c.set(Calendar.YEAR, 2000);
        Date mil2 = c.getTime();
        c.set(Calendar.YEAR, 3000);
        Date mil3 = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 1);
        Date mil3Plus = c.getTime();

        assertThat(DateUtil.compareYear(mil2, mil3), is(false));
        assertThat(DateUtil.compareYear(mil3, mil3Plus), is(true));
    }

    @Test
    public void testShortString() {
        Calendar c = new GregorianCalendar(1992, 0, 11);
        assertThat(DateUtil.toShortString(c.getTime()), is("1992-01-11"));
    }


    @Test
    public void testMonthYearString() {
        Calendar c = new GregorianCalendar(1992, 0, 11);
        assertThat(DateUtil.toMonthYearString(c.getTime()), is("Jan 92"));
    }

    @Test
    public void testYearString() {
        Calendar c = new GregorianCalendar(1992, 0, 11);
        assertThat(DateUtil.toYearString(c.getTime()), is("1992"));
    }


}
