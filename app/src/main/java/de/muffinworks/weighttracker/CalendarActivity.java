package de.muffinworks.weighttracker;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.ui.CalendarView;
import de.muffinworks.weighttracker.ui.WeightDialogFragment;
import de.muffinworks.weighttracker.util.DateUtil;

public class CalendarActivity extends AppCompatActivity
        implements WeightDialogFragment.WeightDialogListener {

    private Toolbar mToolbar;
    private CalendarView mCal;
    private List<Weight> entries;
    private WeightDbService dbService;
    private WeightDialogFragment mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initToolbar();

        dbService = new WeightDbService(this);
        entries = dbService.getAllEntries();

        mCal = (CalendarView)findViewById(R.id.calendar_view);
        mCal.updateCalendar(entries);

        //assign event handler
        mCal.setEventHandler(new CalendarView.EventHandler() {
            @Override
            public void onDayClick(Date date, TextView weightView) {
                showWeightDialog(date, weightView.getText().toString());
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //TODO test on API 17, 18, 19
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(CalendarActivity.this, "Up clicked",
                            Toast.LENGTH_SHORT).show();
                    NavUtils.navigateUpFromSameTask(CalendarActivity.this);
                }
            });
        }

    }

    private void updateCalenderAfterChange(){
        entries = dbService.getAllEntries();
        mCal.updateCalendar(entries);
    }

    //android system stuff
    @Override
    public void onBackPressed() {
        //calling on finish to emulate same behaviour as on home up button press to call oncreateview in parent
        NavUtils.navigateUpFromSameTask(CalendarActivity.this);
    }

    @Override
    protected void onDestroy() {
        dbService.close();
        super.onDestroy();
    }

    //DIALOG STUFF
    private void showWeightDialog(Date date, String weight) {
        mDialog = new WeightDialogFragment();
        mDialog.setWeight(weight);
        mDialog.setDate(date);
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
        updateCalenderAfterChange();
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }

    public void onDialogDismiss(DialogFragment dialog) {}

}
