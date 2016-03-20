package de.muffinworks.weighttracker.db;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by Bianca on 29.02.2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class WeightDbServiceTest {

    private WeightDbService service;

    @Before
    public void setUp() {
        getTargetContext().deleteDatabase(WeightDbHelper.DATABASE_NAME);
        service = new WeightDbService(getTargetContext());
    }


    @Test
    public void testPut() {
        Weight weight = new Weight(DateUtil.currentDate(), 120);
        service.putWeightEntry(weight);
        service.putWeightEntry(weight);
        weight = new Weight(2010,11,11, 123.0);
        service.putWeightEntry(weight);

        List<Weight> weights = service.getAllEntries();
        assertThat(2, is(weights.size()));
        assertThat(120.0, is(weights.get(1).getKilos()));
        assertThat(DateUtil.getDateInteger(DateUtil.currentDate()), is(weights.get(1).getDateInt()));
        assertThat(20101011, is(weights.get(0).getDateInt()));
    }

    @Test
    public void testGet() {
        Weight weight = new Weight(2111, 3, 12, 11.0);
        service.putWeightEntry(weight);
        assertThat(11.0, is(service.get(DateUtil.getDateFromInteger(21110212)).getKilos()));
    }

    @Test
    public void testDelete() {
        Weight weight = new Weight(2011, 1, 22, 333);
        service.putWeightEntry(weight);
        service.deleteEntry(DateUtil.getDateFromInteger(20110022));
        assertThat(null, is(service.get(DateUtil.getDateFromInteger(20110022))));
    }


    @After
    public void tearDown() {
        if(service != null)
            service.close();
    }

}
