package de.muffinworks.weighttracker;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.muffinworks.weighttracker.db.Weight;
import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.util.DateUtil;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.AbstractChartData;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

/**
 * Created by Bianca on 18.04.2016.
 */
public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    private WeightDbService dbService;

    private HashMap<CustomPagerEnum, View> graphs = new HashMap<>();

    public CustomPagerAdapter(Context context) {
        this.mContext = context;
        dbService = new WeightDbService(context);
        updateGraphs();
    }

    private Axis yearAxis;
    private Axis monthAxis;
    private Axis dateAxis;

    private LineChartData data;
    private LineChartData previewData;



    //GRAPH STUFF
    private class ZoomOutAxisChanger implements ViewportChangeListener {

        private LineChartData data;

        public ZoomOutAxisChanger(LineChartData data) {
            this.data = data;
        }

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

    // INIT STUFF
    public void updateGraphs() {
        List<PointValue> values = new ArrayList<>();
        List<Weight> weights = dbService.getAllEntries();

        //fill graph
        for(int i = 0; i < weights.size(); ++i) {
            Weight weight = weights.get(i);
            int x = (int) TimeUnit.DAYS.convert(weight.getDate().getTime(), TimeUnit.MILLISECONDS);
            values.add(new PointValue(x, (float) weight.getKilos()));
        }

        Line dummyline;
        if(weights.size() == 0) {
            createAxis(DateUtil.coolDate(), DateUtil.currentDate());
            dummyline = createDummyPoints(DateUtil.coolDate(), DateUtil.currentDate());
        } else {
            Date earliest = weights.get(0).getDate();
            Date latest = weights.get(weights.size() - 1).getDate();
            if(DateUtil.daysBetween(earliest, latest) < 7) {
                createAxis(DateUtil.oneWeekAgo(), DateUtil.currentDate());
                dummyline = createDummyPoints(DateUtil.oneWeekAgo(), DateUtil.currentDate());
            } else {
                createAxis(earliest, DateUtil.currentDate());
                dummyline = createDummyPoints(earliest, DateUtil.currentDate());
            }
        }

        //PREPARE LINE DATA
        Line line = new Line(values);
        line.setColor(R.color.colorAccent);
        line.setHasPoints(false);

        List<Line> lines = new ArrayList<>();
        lines.add(line);
        lines.add(dummyline);

        data = new LineChartData(lines);
        data.setAxisXBottom(dateAxis);
        data.setAxisYLeft(new Axis()
                .setHasLines(true)
                .setMaxLabelChars(4));

        previewData = new LineChartData(lines);
        previewData.setAxisXBottom(monthAxis);
        previewData.setAxisYLeft(new Axis()
                .setMaxLabelChars(4));
        //previewData.setAxisYLeft(null);

        setAxisColor(data, R.color.black);

        //SET LINE DATA

    }

    private void setDaysLineChart(LineChartView lineChart) {
        lineChart.setLineChartData(data);
        Viewport maxViewport = lineChart.getMaximumViewport();

        int right = (int) maxViewport.right;
        int left = Math.max((int) maxViewport.left, right - 31);
        int top = (int) maxViewport.top+2;

        // Zoom to latest 31 days
        Viewport viewport = new Viewport(left, top, right, 0);
        lineChart.setCurrentViewport(viewport);
        lineChart.setZoomEnabled(false);
        lineChart.setScrollEnabled(false);
    }

    private void setAllLineChart(LineChartView previewLineChart) {
        previewLineChart.setLineChartData(previewData);
        previewLineChart.setViewportChangeListener(new ZoomOutAxisChanger(previewData));
        previewLineChart.setCurrentViewport(previewLineChart.getMaximumViewport());
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

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(customPagerEnum.getLayoutResId(), null);
        collection.addView(view);

        if(customPagerEnum == CustomPagerEnum.ALL) {
            setAllLineChart((LineChartView) view.findViewById(R.id.graph_all));
        } else if(customPagerEnum == CustomPagerEnum.DAYS) {
            setDaysLineChart((LineChartView) view.findViewById(R.id.graph_month));
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View)view);
    }

    @Override
    public int getCount() {
        return CustomPagerEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString((CustomPagerEnum.values()[position]).getTitleResId());
    }

    enum CustomPagerEnum {

        DAYS(R.string.dayGraphTabTitle, R.layout.month_graph_layout),
        ALL(R.string.allTimeGraphTabTitle, R.layout.graph_layout);

        private int mTitleResId;
        private int mLayoutResId;

        CustomPagerEnum(int title, int layoutResId) {
            mTitleResId = title;
            mLayoutResId = layoutResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public int getLayoutResId() {
            return mLayoutResId;
        }
    }
}
