package de.muffinworks.weighttracker.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.muffinworks.weighttracker.R;
import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by Bianca on 17.03.2016.
 */
public class CalendarView extends LinearLayout {

    private static final int DAYS_COUNT = 42;
    private static final String DATE_FORMAT = "Calender View";
    private String dateFormat;
    private Calendar currentDate = Calendar.getInstance();
    private EventHandler eventHandler = null;
    private List<Weight> entries = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;


    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);
        try {
            //try to load previous date format and fall back to default otherwise
            dateFormat = typedArray.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null) {
                dateFormat = DATE_FORMAT;
            }
        } finally {
            typedArray.recycle();
        }
    }

    private void assignUiElements() {
        //assign local variables to components after layout is infalted
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers() {
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (eventHandler == null) {
                    return;
                }
                eventHandler.onDayClick((Date) parent.getItemAtPosition(position));
            }
        });
    }

    //display dates correctly in grid
    public void updateCalendar() {
        updateCalendar(entries);
    }

    public void updateCalendar(List<Weight> entries) {
        this.entries = entries;
        ArrayList<Date> cells = new ArrayList<>();
        Calendar c = (Calendar)currentDate.clone();

        //determine the cell for current months beginning
        c.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = c.get(Calendar.DAY_OF_WEEK) - 1;

        //move calendar back to beginning ot week
        c.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        //fill cells
        while(cells.size() < DAYS_COUNT) {
            cells.add(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        //update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, entries));

        //update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));
    }

    private class CalendarAdapter extends ArrayAdapter<Date> {
        //days with events
        private List<Weight> entries;
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, List<Weight> entries) {
            super(context, R.layout.control_calendar_day, days);
            this.entries = entries;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            //day in question
            Date date = getItem(position);
            Calendar c = Calendar.getInstance();
            c.setTime(date);

            //today
            Date today = DateUtil.currentDate();

            //inflate item if it does not exist yet
            if (view == null) {
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);
            }

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.BLACK);

            if (!DateUtil.compareMonth(date, today) || !DateUtil.compareYear(date, today))
            {
                // if this day is outside current month, grey it out
//                ((TextView)view).setTextColor(getResources().getColor(R.color.greyed_out, null)); //???
                ((TextView)view).setTextColor(ContextCompat.getColor(getContext(), R.color.greyed_out)); //???
            }
            else if (DateUtil.compareDay(date, today))
            {
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);
                ((TextView)view).setTextColor(getResources().getColor(R.color.today));
            }

            // set text
            ((TextView)view).setText(String.valueOf(DateUtil.getDayOfMonth(date)));

            //if this day has a weight
            if (entries != null) {
                //arraylist is sorted by date integer in db
                int index = Collections.binarySearch(entries, new Weight(date));
                if (index > 0) {
                    ((TextView)view).setTextColor(Color.CYAN);
                }
            }

            return view;
        }
    }

    //assign event handler
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onDayClick(Date date);
    }
}
