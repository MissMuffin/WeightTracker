package de.muffinworks.weighttracker;

import android.app.DialogFragment;
import android.app.admin.DeviceAdminInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.ui.WeightDialogFragment;
import de.muffinworks.weighttracker.util.Constants;
import de.muffinworks.weighttracker.util.DateUtil;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.AbstractChartData;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.AbstractChartView;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity
    implements WeightDialogFragment.WeightDialogListener {

    private FloatingActionButton fab;
    private TextView mCurrentWeight;
    private WeightDbService dbService;
    private WeightDialogFragment mDialog;
    private Weight mTodayWeight;

    private LineChartView daysLineChart;
    private ColumnChartView monthsColumnChart;
    private LineChartData daysData;
    private ColumnChartData monthsData;

    //used to store linegraph data for each month
    private List<List<PointValue>> lineGraphsForMonths = new ArrayList<List<PointValue>>();
    //stores axis values for days in each month
    private List<List<AxisValue>> axisValuesForMonths = new ArrayList<List<AxisValue>>();

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
        initGraph();
    }

    //INIT STUFF
    private void initGraph() {
        //display current week number, month name and year below in textview
        daysLineChart = (LineChartView) findViewById(R.id.line_chart);
        monthsColumnChart = (ColumnChartView) findViewById(R.id.column_chart);

        //months column init
        int numColumns = Calendar.getInstance(Locale.getDefault()).get(Calendar.MONTH) + 1;
        List<AxisValue> monthsAxisValues = new ArrayList<>();
        List<Column> columns = new ArrayList<>();

        for (int i = 0; i < numColumns; i++) {
            List<Weight> weightsForMonth = dbService.getMonthFilled(Constants.MONTHS[i]);
            List<AxisValue> daysAxisValues = new ArrayList<>();

            //setup column with label showing average for month and add it to columns list:
            monthsAxisValues.add(new AxisValue(i).setLabel(Constants.MONTHS_STRINGS[i]));
            List<SubcolumnValue> average = new ArrayList<>();
            average.add(new SubcolumnValue((float)Weight.getAverage(weightsForMonth)));
            columns.add(new Column(average).setHasLabelsOnlyForSelected(true));

            //setup line graph for month
            List<PointValue> points = new ArrayList<>();
            for (int j = 0; j < weightsForMonth.size(); j++) {
                //add data point
                points.add(new PointValue(j, (float)weightsForMonth.get(i).getKilos()));
                //add label for data point
                daysAxisValues.add(new AxisValue(j).setLabel(Integer.toString(j+1)));
            }
            //add list of data points for current month to list
            lineGraphsForMonths.add(points);
            //add list of axis values for current month
            axisValuesForMonths.add(daysAxisValues);
        }

        //PREPARE COLUMN DATA
        monthsData = new ColumnChartData(columns);
        monthsData.setAxisXBottom(new Axis(monthsAxisValues));
        setAxisColor(monthsData, R.color.black);

        //SET COLUMNS
        monthsColumnChart.setColumnChartData(monthsData);
        // Set selection mode to keep selected month column highlighted.
        monthsColumnChart.setValueSelectionEnabled(true);
        monthsColumnChart.setZoomType(ZoomType.HORIZONTAL);
        monthsColumnChart.setMaxZoom(4);
        //set to current month as x value
        monthsColumnChart.setZoomLevel(monthsData.getColumns().size() - 1, 0, 2);

        // Set value touch listener that will trigger changes for line graph.
        monthsColumnChart.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                //TODO set line graph for day weight values in selected month
                setLineGraph(columnIndex);
            }

            @Override
            public void onValueDeselected() {
            }
        });

        //TODO set current month as selected
//        monthsColumnChart.getOnValueTouchListener().onValueSelected(DateUtil.getCurrentMonthIndex(),
//                0, null);

        //PREPARE LINE DATA
        Line line = new Line(lineGraphsForMonths.get(DateUtil.getCurrentMonthIndex()));
        line.setColor(R.color.colorAccent);
        List<Line> lines = new ArrayList<>();
        lines.add(line);

        daysData = new LineChartData();
        daysData.setLines(lines);
        daysData.setAxisXBottom(new Axis(axisValuesForMonths.get(DateUtil.getCurrentMonthIndex()))
                .setHasLines(true));
        setAxisColor(daysData, R.color.black);
        daysData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

        //SET LINE DATA
        int daysInMonth = DateUtil.getDaysInMonth(DateUtil.getCurrentMonthIndex());
        daysLineChart.setLineChartData(daysData);

        // For build-up animation you have to disable viewport recalculation.
        daysLineChart.setViewportCalculationEnabled(false);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v = new Viewport(
                0,
                110,
                (float)daysInMonth,
                0
        );
        daysLineChart.setMaximumViewport(v);
        daysLineChart.setCurrentViewport(v);
        daysLineChart.setZoomLevel(daysInMonth, 0, 4);
        daysLineChart.setZoomEnabled(true);
        daysLineChart.setZoomType(ZoomType.HORIZONTAL);
        daysLineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
    }

    private void setAxisColor(AbstractChartData data, int color) {
        Axis top = data.getAxisXTop();
        Axis right = data.getAxisYRight();
        Axis bottom = data.getAxisXBottom();
        Axis left = data.getAxisYLeft();

        if (top!=null) top.setTextColor(color).setLineColor(color);
        if (right!=null) right.setTextColor(color).setLineColor(color);
        if (bottom!=null) bottom.setTextColor(color).setLineColor(color);
        if (left!=null) left.setTextColor(color).setLineColor(color);
    }

    private void setLineGraph(int monthIndex) {
        // Cancel last animation if not finished.
        daysLineChart.cancelDataAnimation();
        // Modify data targets
        Line line = daysData.getLines().get(0);//there is always only one line at any given time
        line.setColor(R.color.colorAccent);
        line.setValues(lineGraphsForMonths.get(monthIndex));

        Viewport v = daysLineChart.getCurrentViewport();
        v.set(0, 110, (float)axisValuesForMonths.get(DateUtil.getCurrentMonthIndex()).size(), 0);
        daysLineChart.setMaximumViewport(v);
        daysLineChart.setCurrentViewport(v);

        // Start new data animation with 300ms duration;
        daysLineChart.startDataAnimation(300);
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
        if (id == R.id.settings_add_test_data) {
            showSnackbar("TESST");
            return true;
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
        if (weight==-1) {
            dbService.deleteEntry(date); //empty input
        } else {
            dbService.putWeightEntry(new Weight(date, weight));
        }
        updateCurrentWeightText();
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
}
