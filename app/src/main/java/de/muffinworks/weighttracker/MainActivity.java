package de.muffinworks.weighttracker;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.services.AlarmReceiver;
import de.muffinworks.weighttracker.ui.SetNotificationFragment;
import de.muffinworks.weighttracker.ui.WeightDialogFragment;
import de.muffinworks.weighttracker.util.ConfigUtil;
import de.muffinworks.weighttracker.util.DateUtil;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.hack.HackyViewPager;

public class MainActivity extends AppCompatActivity
    implements WeightDialogFragment.WeightDialogListener,
        SetNotificationFragment.NotificationFragmentListener {

    private FloatingActionButton fab;
    private TextView mCurrentWeight;
    private WeightDbService dbService;
    private WeightDialogFragment mDialog;
    private Weight mTodayWeight;




    private ConfigUtil config;

    private HackyViewPager viewPager;
    private CustomPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open calendar activity
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        dbService = new WeightDbService(this);

        initCurrentWeight();

        config = new ConfigUtil(this);
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new CustomPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        updateGraph();
    }








    private void updateGraph() {
        viewPagerAdapter.updateGraphs();
    }

    //CURRENT WEIGHT STUFF
    private void initCurrentWeight() {
        mCurrentWeight = (TextView) findViewById(R.id.currentDayWeight);

        updateCurrentWeightText();

        mCurrentWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeightDialog();
            }
        });
    }

    private void updateCurrentWeightText() {
        mTodayWeight = dbService.get(DateUtil.currentDate());
        if (mTodayWeight != null) {
            mCurrentWeight.setText(mTodayWeight.getKilos() + " kg");
            mCurrentWeight.setTextSize(TypedValue.COMPLEX_UNIT_PT, 25);
        } else {
            mCurrentWeight.setText("Click to enter \nyour weight for today");
            mCurrentWeight.setTextSize(25);
        }
    }

    //NECESSARY ANDROID SYSTEM STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.settings_add_test_data) {
            // Create dummy data.
            dbService.createDummyEntries();
            updateGraph();
            updateCurrentWeightText();
            showSnackbar("Created dummy data!");
            return true; // why return true? -> to tell system that event has been handled and it can keep its hands off
        }
        else if(id == R.id.action_delete_data) {
            dbService.clearAll();
            updateGraph();
            updateCurrentWeightText();
            showSnackbar("Deleted all the data!");
            return true;
        }
        if (id == R.id.action_set_notification) {
            SetNotificationFragment dialog = new SetNotificationFragment();
            dialog.setTime(Math.max(config.getReminderHour(), 0), Math.max(config.getReminderMinute(),0));
            dialog.show(getFragmentManager(), "timer dialog");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dbService.close();
        super.onDestroy();
    }

    //DIALOG STUFF
    private void showWeightDialog() {
        mDialog = new WeightDialogFragment();
        if (mTodayWeight != null) mDialog.setWeight(Double.toString(mTodayWeight.getKilos()));
        mDialog.show(getFragmentManager(), "weight");
    }

   //DIALOG LISTENER
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, Date date, double weight) {
        if (weight <= 0) {
            dbService.deleteEntry(date); //empty input
        } else {
            dbService.putWeightEntry(new Weight(date, weight));
        }
        updateCurrentWeightText();
        updateGraph();
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        showToast("cancel");
        dialog.getDialog().cancel();
    }

    public void onDialogDismiss(DialogFragment dialog) {
        showToast("dismiss");
    }

    //LOGGING
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showSnackbar(String message) {
        Snackbar.make(this.findViewById(R.id.fab), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void SetReminder(int hour, int minute) {
        config.setReminderTime(hour, minute);
        Log.i("MainActivity", "Reminder time set to " + hour + ":" + minute);
        //RemoveReminder();
        new AlarmReceiver().setAlarm(getApplicationContext(), config.getReminderHour(), config.getReminderMinute());
    }

    @Override
    public void DisableReminder() {
        config.setReminderTime(-1, -1);
        new AlarmReceiver().clearAlarm(getApplicationContext());
    }


}
