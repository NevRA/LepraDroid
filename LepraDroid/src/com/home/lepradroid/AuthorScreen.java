package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class AuthorScreen extends BaseActivity
{
    private String          username;
    private ArrayList<BaseView>
                            pages                   = new ArrayList<BaseView>();
    private UUID            authorPostsGroupId      = UUID.randomUUID();

    ViewPager               pager;

    public static final int AUTHOR_TAB_NUM          = 0;
    public static final int POSTS_TAB_NUM           = 1;

    private boolean postsInit                       = false;

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
        authorView.setTag(Utils.getString(R.string.Author_Tab));

        PostsScreen authorPostsView = new PostsScreen(
                this,
                authorPostsGroupId,
                String.format(Commons.AUTHOR_POSTS_URL, username),
                Commons.PostsType.USER,
                Utils.getString(R.string.AuthorPosts_Tab));
        authorPostsView.setTag(Utils.getString(R.string.AuthorPosts_Tab));
        
        pages.add(authorView);
        pages.add(authorPostsView);

        TabsPageAdapter tabsAdapter = new TabsPageAdapter(pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);

        titleIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener()
                {
                    @Override
                    public void onPageSelected(int position)
                    {
                        switch(position)
                        {
                            case POSTS_TAB_NUM:
                                if(!postsInit)
                                {
                                    reloadPostsTab();
                                    postsInit = true;
                                }
                                break;
                        }
                    }

                    @Override
                    public void onPageScrolled(int position,
                                               float positionOffset, int positionOffsetPixels)
                    {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state)
                    {
                    }
                });

        reloadAuthorTab();
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
            switch (pager.getCurrentItem())
            {
                case AUTHOR_TAB_NUM:
                    reloadAuthorTab();
                    break;
                case POSTS_TAB_NUM:
                    reloadPostsTab();
                    break;
            }

            break;
        case MENU_INBOX:
            Utils.addInbox(this, username);
            break;
        }
        return false;
    }

    private void reloadAuthorTab()
    {
        pushNewTask(new TaskWrapper(null, new GetAuthorTask(username), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }

    private void reloadPostsTab()
    {
        pushNewTask(new TaskWrapper(null,
                new GetPostsTask(
                        authorPostsGroupId,
                        String.format(Commons.AUTHOR_POSTS_URL, username),
                        Commons.PostsType.USER),
                Utils.getString(R.string.Posts_Loading_In_Progress)));
    }
}
