package com.home.lepradroid;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class AuthorScreen extends BaseActivity
{
    private String          username;
    private TitlePageIndicator 
                            titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ViewPager       pager;
    private ArrayList<BaseView> 
                            pages = new ArrayList<BaseView>();
    private AuthorView      authorView;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_base_view);
        
        username    = getIntent().getExtras().getString("username");

        createTabs();
    }
    private void createTabs()
    {
        authorView = new AuthorView(this, username);
        authorView.setTag(username);
        
        pages.add(authorView);
        
        tabsAdapter = new TabsPageAdapter(this, pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
        
        pushNewTask(new TaskWrapper(null, new GetAuthorTask(username), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            pushNewTask(new TaskWrapper(null, new GetAuthorTask(username), Utils.getString(R.string.Posts_Loading_In_Progress)));
            break;
        }
        return false;
    }
}
