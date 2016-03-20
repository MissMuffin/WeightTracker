package de.muffinworks.weighttracker.db;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import de.muffinworks.weighttracker.util.DateUtil;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Bianca on 29.02.2016.
 */
@SmallTest
public class WeightTest {

    @Test
    public void testDateIntStuff() {
        Date date = new Weight(2010, 2, 1, 13.0).getDate();
        int asInt = DateUtil.getDateInteger(date);
        assertThat(20100101, is(asInt));
        assertThat(date, is(DateUtil.getDateFromInteger(asInt)));
    }

    @Test
    public void testCompareTo() {
        ArrayList<Weight> list = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 16);
        int count = 0;
        do {
            list.add(new Weight(c.getTime(), count));
            count++;
            c.add(Calendar.DAY_OF_MONTH, -1);
        }while (count < 15);

        for (Weight w : list) {
            Log.d("testCompareToBEFORE", "date: " + w.getDateInt() + " weight: " + w.getKilos());
        }
        Collections.sort(list);
        for (Weight w : list) {
            Log.d("testCompareToAFTER", "date: " + w.getDateInt() + " weight: " + w.getKilos());
        }
    }

    @Test
    public void testBinarySearch() {
        ArrayList<Weight> list = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 16);
        int count = 0;
        do {
            list.add(new Weight(c.getTime(), count));
            count++;
            c.add(Calendar.DAY_OF_MONTH, -1);
        }while (count < 15);

        Collections.sort(list);
        c.set(Calendar.DAY_OF_MONTH, 7);
        Weight searchObj = new Weight(c.getTime());
        Weight foundObj = list.get(Collections.binarySearch(list, searchObj));
        assertThat("should be the same", searchObj.getDateInt(), is(foundObj.getDateInt()));
    }
}
