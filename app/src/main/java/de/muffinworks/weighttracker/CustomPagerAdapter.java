package de.muffinworks.weighttracker;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Bianca on 18.04.2016.
 */
public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;

    public CustomPagerAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(customPagerEnum.getmLayoutResId(), null);
        collection.addView(view);
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

    //ENUM
    enum CustomPagerEnum {

        DAYS(R.string.dayGraphTabTitle, R.layout.graph_layout),
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

        public int getmLayoutResId() {
            return mLayoutResId;
        }
    }
}
