package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class BlogScreen extends BaseActivity
{
    private UUID groupId;
    private UUID id;
    private String title;
    private PostsScreen postsScreen;
    private BaseItem post;
    private TitlePageIndicator titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ViewPager pager;
    private ArrayList<BaseView> pages = new ArrayList<BaseView>();
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_view);

        try
        {
            groupId = UUID.fromString(getIntent().getExtras().getString("groupId"));
            id = UUID.fromString(getIntent().getExtras().getString("id"));
            title = getIntent().getExtras().getString("title");

            postsScreen = new PostsScreen(this, id, title);
            postsScreen.setTag(title);
            
            pages.add(postsScreen);
            
            tabsAdapter = new TabsPageAdapter(this, pages);
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(tabsAdapter);
            pager.setCurrentItem(0);

            titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
            titleIndicator.setViewPager(pager);
            titleIndicator.setCurrentItem(0);
            
            post = ServerWorker.Instance().getPostById(groupId, id);
            if(post != null)
                pushNewTask(new TaskWrapper(null, new GetPostsTask(id, post.Url), Utils.getString(R.string.Posts_Loading_In_Progress)));
            else
                finish(); // TODO message
        }
        catch (Exception e)
        {
            Utils.showError(this, e);
        }
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId())
        {
        case MENU_RELOAD:
            pushNewTask(new TaskWrapper(null, new GetPostsTask(id, post.Url), Utils.getString(R.string.Posts_Loading_In_Progress)));
            return true;
        }
        return false;
    }
    
    @Override
    protected void onDestroy()
    {
        if(postsScreen != null)
        {
            postsScreen.OnExit();
            unbindDrawables(postsScreen.contentView);
        }
        super.onDestroy();
    }
}
