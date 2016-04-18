package de.muffinworks.weighttracker.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by tethik on 17/04/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class NotifyServiceTest {

    private NotifyService service;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void setUp() throws TimeoutException {

    }

    @Test
    @Ignore
    public void testCreateNotification() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(),
                        NotifyService.class);

        mServiceRule.startService(serviceIntent);
    }

}
