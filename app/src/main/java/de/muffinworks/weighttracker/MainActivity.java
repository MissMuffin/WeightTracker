package de.muffinworks.weighttracker;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.services.AlarmReceiver;
import de.muffinworks.weighttracker.services.NotifyService;
import de.muffinworks.weighttracker.ui.SetNotificationFragment;
import de.muffinworks.weighttracker.ui.WeightDialogFragment;
import de.muffinworks.weighttracker.util.ConfigUtil;
import de.muffinworks.weighttracker.util.DateUtil;

public class MainActivity extends AppCompatActivity
    implements WeightDialogFragment.WeightDialogListener,
        SetNotificationFragment.NotificationFragmentListener {

    private FloatingActionButton fab;
    private TextView mCurrentWeight;
    private WeightDbService dbService;
    private WeightDialogFragment mDialog;
    private Spinner mSpinner;
    private GraphView mGraph;
    private TextView mCurrentTime;
    private Weight mTodayWeight;

    private ConfigUtil config;
    private String selectedTimePeriod = null;

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
        config = new ConfigUtil(this);
        selectedTimePeriod = config.getTimePeriod();


        initCurrentWeight();
        initSpinner();
        updateGraph();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private List<Weight> getEntriesForSelectedTime() {
        Date[] startEnd = getStartAndEndDatesForSelectedTime();

        if(startEnd == null)
            return dbService.getAllEntries();
        return dbService.get(startEnd[0], startEnd[1]);
    }

    private Date[] getStartAndEndDatesForSelectedTime() {
        Date today = DateUtil.currentDate();
        Calendar c = Calendar.getInstance();
        c.setTime(today);

        selectedTimePeriod = (String) mSpinner.getSelectedItem();

        switch(selectedTimePeriod) {
            case "Week":
                c.add(Calendar.DATE, -7);
                return new Date[] { c.getTime(), today };
            default:
            case "Month":
                c.add(Calendar.MONTH, -1);
                return new Date[] { c.getTime(), today };
            case "Year":
                c.add(Calendar.YEAR, -1);
                return new Date[] { c.getTime(), today };
            case "All time":
                return null;
        }
    }

    //INIT STUFF
    private void updateGraph() {
        //display current week number, month name and year below in textview
        mCurrentTime = (TextView) findViewById(R.id.current_time_period);

        mGraph = (GraphView) findViewById(R.id.graph);

        List<Weight> weights = getEntriesForSelectedTime();

        mGraph.removeAllSeries();
        DataPoint[] values = new DataPoint[weights.size()];

        for(int i = 0; i < weights.size(); ++i) {
            Weight w = weights.get(i);
            values[i] = new DataPoint(w.getDate(), w.getKilos());
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(values);

        if(weights.size() > 0)
            mGraph.addSeries(series);

        // set date label formatter
        mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        mGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

        Date[] startEnd = getStartAndEndDatesForSelectedTime();
        mGraph.getViewport().setXAxisBoundsManual(true);
        if(startEnd != null) {
            mGraph.getViewport().setMinX(startEnd[0].getTime());
            mGraph.getViewport().setMaxX(startEnd[1].getTime());
        } else {
            if(weights.size() > 1) {
                Weight first = weights.get(0);
                Weight last = weights.get(weights.size() - 1);
                mGraph.getViewport().setMinX(first.getDate().getTime());
                mGraph.getViewport().setMaxX(last.getDate().getTime());
            } else {
                Calendar c = Calendar.getInstance();
                c.set(1992, 1, 11);
                mGraph.getViewport().setMinX(c.getTime().getTime());
                c.set(2052, 1, 11);
                mGraph.getViewport().setMaxX(c.getTime().getTime());
            }
        }

        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(0);
    }

    private void initSpinner() {
        mSpinner = (Spinner) findViewById(R.id.spinner_time_period);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_period_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        if(selectedTimePeriod != null)
            mSpinner.setSelection(adapter.getPosition(selectedTimePeriod));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                showToast(parent.getItemAtPosition(position).toString());
                config.setTimePeriod((String) mSpinner.getSelectedItem());
                updateGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

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
            return true; // why return true?
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
            dialog.show(getFragmentManager(), "wut");
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

    //SPINNER ON ITEM CLICK HANDLING

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
