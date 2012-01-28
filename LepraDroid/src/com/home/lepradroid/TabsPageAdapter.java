package com.home.lepradroid;

import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.home.lepradroid.base.BaseView;
import com.viewpagerindicator.TitleProvider;

public class TabsPageAdapter extends PagerAdapter implements TitleProvider
{
    private List<BaseView> pages;

    public TabsPageAdapter(Context context, List<BaseView> pages)
    {
        this.pages = pages;
    }

    @Override
    public Object instantiateItem(View pCollection, int pPosition)
    {
        View view = pages.get(pPosition).contentView;
        ((ViewPager) pCollection).addView(view, 0);
        
        return view;
    }

    @Override
    public void destroyItem(View pCollection, int pPosition, Object pView)
    {
        ((ViewPager) pCollection).removeView((View)pView);
    }

    @Override
    public int getCount()
    {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View pView, Object pObject)
    {
        return pView.equals(pObject);
    }

    @Override
    public void finishUpdate(View pView)
    {
    }

    @Override
    public void restoreState(Parcelable pParcelable, ClassLoader pLoader)
    {
    }

    @Override
    public Parcelable saveState()
    {
        return null;
    }

    @Override
    public void startUpdate(View pView)
    {
    }

    /**
     * For TitleProvider
     */
    public String getTitle(int pPosition)
    {
        return (String) (pages.get(pPosition).getTag());
    }
}