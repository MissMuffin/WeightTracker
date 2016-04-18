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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.services.AlarmReceiver;
import de.muffinworks.weighttracker.ui.SetNotificationFragment;
import de.muffinworks.weighttracker.ui.WeightDialogFragment;
import de.muffinworks.weighttracker.util.ConfigUtil;
import de.muffinworks.weighttracker.util.DateUtil;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.AbstractChartData;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import lecho.lib.hellocharts.view.hack.HackyViewPager;

public class MainActivity extends AppCompatActivity
    implements WeightDialogFragment.WeightDialogListener,
        SetNotificationFragment.NotificationFragmentListener {

    private FloatingActionButton fab;
    private TextView mCurrentWeight;
    private WeightDbService dbService;
    private WeightDialogFragment mDialog;
    private Weight mTodayWeight;


    private LineChartView lineChart;
    private PreviewLineChartView previewLineChart;
    private LineChartData data;
    private LineChartData previewData;

    private Axis yearAxis;
    private Axis monthAxis;
    private Axis dateAxis;

    private ConfigUtil config;
    private String selectedTimePeriod = null;

    private HackyViewPager viewPager;


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
//        updateGraph();
        config = new ConfigUtil(this);
        selectedTimePeriod = config.getTimePeriod();

        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
    }

    //GRAPH STUFF
    private class ZoomOutAxisChanger implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport viewport) {
            int width = (int) viewport.right - (int) viewport.left;
            Log.d("Viewport", "changed to " + width);
            if (width < 90) {
                data.setAxisXBottom(dateAxis);
            } else if (width > 90) {
                data.setAxisXBottom(monthAxis);
            } else if (width > 730) {
                data.setAxisXBottom(yearAxis);
            }
        }
    }

    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            lineChart.setCurrentViewport(newViewport);
        }

    }

    private void createAxis(Date startDate, Date endDate) {
        List<AxisValue> dateAxisValues = new ArrayList<>();
        List<AxisValue> monthAxisValues = new ArrayList<>();
        List<AxisValue> yearAxisValues = new ArrayList<>();

        int minX = DateUtil.getDateInteger(startDate);
        int maxX = DateUtil.getDateInteger(endDate)+2;

        Log.d("Chart axis stuff", "creating axis for " + minX + " to " + maxX);
        for(int x = minX; x < maxX; ++x) {
            AxisValue dateAxisValue = new AxisValue(x).setLabel(DateUtil.toShortString(DateUtil.getDateFromInteger(x)));
            dateAxisValues.add(dateAxisValue);

            AxisValue monthAxisValue = new AxisValue(x).setLabel(DateUtil.toMonthYearString(DateUtil.getDateFromInteger(x)));
            monthAxisValues.add(monthAxisValue);

            AxisValue yearAxisValue = new AxisValue(x).setLabel(DateUtil.toYearString(DateUtil.getDateFromInteger(x)));
            yearAxisValues.add(yearAxisValue);
        }

        yearAxis = new Axis(yearAxisValues)
                .setMaxLabelChars(4);

        monthAxis = new Axis(monthAxisValues)
                .setMaxLabelChars(7);

        dateAxis = new Axis(dateAxisValues)
                .setMaxLabelChars(10)
                .setHasLines(true);
    }

    private Line createDummyPoints(Date startDate, Date endDate) {
        int minX = DateUtil.getDateInteger(startDate);
        int maxX = DateUtil.getDateInteger(endDate)+2;

        List<PointValue> values = new ArrayList<>();

        for(int x = minX; x < maxX; ++x) {
            values.add(new PointValue(x, 0.0f));
        }

        Line line = new Line(values);
        line.setHasPoints(false);
        line.setColor(0x00000000);
        return line;
    }

    //INIT STUFF
//    private void updateGraph() {
//        // display current week number, month name and year below in textview
//        lineChart = (LineChartView) findViewById(R.id.line_chart);
//        previewLineChart = (PreviewLineChartView) findViewById(R.id.preview_line_chart);
//
//        List<PointValue> values = new ArrayList<>();
//        List<Weight> weights = dbService.getAllEntries();
//
//        //fill graph
//        for(int i = 0; i < weights.size(); ++i) {
//            Weight weight = weights.get(i);
//            int x = (int) TimeUnit.DAYS.convert(weight.getDate().getTime(), TimeUnit.MILLISECONDS);
//            values.add(new PointValue(x, (float) weight.getKilos()));
//        }
//
//        Line dummyline;
//        if(weights.size() == 0) {
//            createAxis(DateUtil.coolDate(), DateUtil.currentDate());
//            dummyline = createDummyPoints(DateUtil.coolDate(), DateUtil.currentDate());
//        } else {
//            Date earliest = weights.get(0).getDate();
//            Date latest = weights.get(weights.size() - 1).getDate();
//            if(DateUtil.daysBetween(earliest, latest) < 7) {
//                createAxis(DateUtil.oneWeekAgo(), DateUtil.currentDate());
//                dummyline = createDummyPoints(DateUtil.oneWeekAgo(), DateUtil.currentDate());
//            } else {
//                createAxis(earliest, DateUtil.currentDate());
//                dummyline = createDummyPoints(earliest, DateUtil.currentDate());
//            }
//        }
//
//        //PREPARE LINE DATA
//        Line line = new Line(values);
//        line.setColor(R.color.colorAccent);
//        line.setHasPoints(false);
//
//        List<Line> lines = new ArrayList<>();
//        lines.add(line);
//        lines.add(dummyline);
//
//        data = new LineChartData(lines);
//        data.setAxisXBottom(dateAxis);
//        data.setAxisYLeft(new Axis()
//                .setHasLines(true)
//                .setMaxLabelChars(4));
//
//        previewData = new LineChartData(lines);
//        previewData.setAxisXBottom(monthAxis);
//        previewData.setAxisYLeft(new Axis()
//                .setMaxLabelChars(4));
//        //previewData.setAxisYLeft(null);
//
//        setAxisColor(data, R.color.black);
//
//        //SET LINE DATA
//        lineChart.setLineChartData(data);
//        previewLineChart.setLineChartData(previewData);
//
//        lineChart.setViewportChangeListener(new ZoomOutAxisChanger());
//        previewLineChart.setViewportChangeListener(new ViewportListener());
//        lineChart.setPadding(25, 25, 25, 25);
//        previewLineChart.setPadding(25, 25, 25, 25);
//
//
//        // For build-up animation you have to disable viewport recalculation.
//        //lineChart.setViewportCalculationEnabled(false);
//
//        Viewport maxViewport = lineChart.getMaximumViewport();
//
//        int right = (int) maxViewport.right;
//        int left = Math.max((int) maxViewport.left, right - 31);
//        int top = (int) maxViewport.top+2;
//
//        // Zoom to latest 31 days
//        Viewport viewport = new Viewport(left, top, right, 0);
//        previewLineChart.setCurrentViewport(viewport);
//        lineChart.setCurrentViewport(viewport);
//
//        lineChart.setZoomEnabled(false);
//        lineChart.setScrollEnabled(false);
//
//        previewLineChart.setZoomType(ZoomType.HORIZONTAL);
//        previewLineChart.setZoomEnabled(true);
//    }
//
//    private void setAxisColor(AbstractChartData data, int color) {
//        Axis top = data.getAxisXTop();
//        Axis right = data.getAxisYRight();
//        Axis bottom = data.getAxisXBottom();
//        Axis left = data.getAxisYLeft();
//
//        if (top!=null) top.setTextColor(color).setLineColor(color);
//        if (right!=null) right.setTextColor(color).setLineColor(color);
//        if (bottom!=null) bottom.setTextColor(color).setLineColor(color);
//        if (left!=null) left.setTextColor(color).setLineColor(color);
//    }

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
//            updateGraph();
            updateCurrentWeightText();
            showSnackbar("Created dummy data!");
            return true; // why return true? -> to tell system that event has been handled and it can keep its hands off
        }
        else if(id == R.id.action_delete_data) {
            dbService.clearAll();
//            updateGraph();
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
//        updateGraph();
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
