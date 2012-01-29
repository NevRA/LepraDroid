package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.tasks.GetCommentsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;
import com.viewpagerindicator.TitlePageIndicator;

public class PostScreen extends BaseActivity
{
    private UUID            groupId;
    private UUID            id;
    private PostView        postView;
    private CommentsView    commentsView;
    private AuthorView      authorView;
    private TitlePageIndicator 
                            titleIndicator;
    private TabsPageAdapter tabsAdapter;
    private ViewPager       pager;
    private ArrayList<BaseView> 
                            pages = new ArrayList<BaseView>();
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_base_view);
        
        groupId = UUID.fromString(getIntent().getExtras().getString("groupId"));
        id = UUID.fromString(getIntent().getExtras().getString("id"));

        createTabs();
    }
    
    private void createTabs()
    {
        postView = new PostView(this, groupId, id);
        postView.setTag(Utils.getString(R.string.Post_Tab));
        
        commentsView = new CommentsView(this, groupId, id);
        commentsView.setTag(Utils.getString(R.string.Comments_Tab));
        
        authorView = new AuthorView(this);
        authorView.setTag(Utils.getString(R.string.Author_Tab));
        
        pages.add(postView);
        pages.add(commentsView);
        pages.add(authorView);
        
        tabsAdapter = new TabsPageAdapter(this, pages);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(tabsAdapter);
        pager.setCurrentItem(0);

        titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        titleIndicator.setCurrentItem(0);
        
        titleIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener()
                {
                    @Override
                    public void onPageSelected(int position)
                    {
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
        
        pushNewTask(new TaskWrapper(null, new GetCommentsTask(groupId, id), Utils.getString(R.string.Posts_Loading_In_Progress)));
    }
}
