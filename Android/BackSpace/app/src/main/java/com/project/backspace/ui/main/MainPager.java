package com.project.backspace.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.project.backspace.R;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class MainPager extends PagerAdapter {
    private Context mcontext = null;
    public MainPager(){
    }
    public MainPager(Context context){
        mcontext = context;
    }
    @Override
    public int getCount(){
        return 2;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position){
        View pageView = null;
        LayoutInflater inflater = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mcontext != null){
            if(position == 0) {
                pageView = inflater.inflate(R.layout.page_first, container, false);
            }else if(position == 1){
                pageView = inflater.inflate(R.layout.page_second, container, false);
            }
        }
        container.addView(pageView);
        return pageView;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
