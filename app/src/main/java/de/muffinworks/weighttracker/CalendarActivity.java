package de.muffinworks.weighttracker;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.ui.CalendarView;

public class CalendarActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CalendarView mCal;
    private List<Weight> entries;
    private WeightDbService dbService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        dbService = new WeightDbService(this);
        entries = dbService.getAllEntries();

        mCal = (CalendarView)findViewById(R.id.calendar_view);
        mCal.updateCalendar(entries);

        //assign event handler
        mCal.setEventHandler(new CalendarView.EventHandler() {
            @Override
            public void onDayClick(Date date) {
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(CalendarActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
