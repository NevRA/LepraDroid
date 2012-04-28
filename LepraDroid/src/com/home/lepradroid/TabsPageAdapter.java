package com.home.lepradroid;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.home.lepradroid.base.BaseView;
import com.viewpagerindicator.TitleProvider;

import java.util.List;

public class TabsPageAdapter extends PagerAdapter implements TitleProvider
{
    private List<BaseView> pages;

    public TabsPageAdapter(List<BaseView> pages)
    {
        this.pages = pages;
    }

    @Override
    public Object instantiateItem(ViewGroup pCollection, int pPosition)
    {
        View view = pages.get(pPosition).contentView;
        pCollection.addView(view, 0);
        
        return view;
    }

    @Override
    public void destroyItem(ViewGroup pCollection, int pPosition, Object pView)
    {
        pCollection.removeView((View)pView);
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
    public void restoreState(Parcelable pParcelable, ClassLoader pLoader)
    {
    }

    @Override
    public Parcelable saveState()
    {
        return null;
    }

    /**
     * For TitleProvider
     */
    public String getTitle(int pPosition)
    {
        return (String) (pages.get(pPosition).getTag());
    }
}