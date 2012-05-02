package com.home.lepradroid;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
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
    private ArrayList<BaseView>
                            pages = new ArrayList<BaseView>();

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
        AuthorView authorView = new AuthorView(this, username);
        authorView.setTag(username);
        
        pages.add(authorView);

        TabsPageAdapter tabsAdapter = new TabsPageAdapter(pages);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
        
        pushNewTask(new TaskWrapper(null, new GetAuthorTask(username), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        menu.add(0, MENU_RELOAD, 0, Utils.getString(R.string.Reload_Menu)).setIcon(R.drawable.ic_reload);
        menu.add(0, MENU_INBOX, 1, Utils.getString(R.string.Inbox_Menu)).setIcon(R.drawable.ic_inbox);

        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            pushNewTask(new TaskWrapper(null, new GetAuthorTask(username), Utils.getString(R.string.Posts_Loading_In_Progress)));
            break;
        case MENU_INBOX:
            Utils.addInbox(this, username);
            break;
        }
        return false;
    }
}
