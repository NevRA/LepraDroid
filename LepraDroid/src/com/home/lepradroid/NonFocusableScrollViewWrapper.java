package com.home.lepradroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class NonFocusableScrollViewWrapper extends ScrollView
{
    public NonFocusableScrollViewWrapper(Context context)
    {
        super(context);
    }

    public NonFocusableScrollViewWrapper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public NonFocusableScrollViewWrapper(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override public void requestChildFocus(View child, View focused)
    {
        return;
    }
}