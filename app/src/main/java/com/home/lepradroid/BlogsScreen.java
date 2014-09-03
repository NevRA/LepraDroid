package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.BlogsUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetBlogsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class BlogsScreen extends BaseView implements BlogsUpdateListener
{
    private ListView        list;
    private ProgressBar     progress;
    public  BlogsAdapter    adapter;
    private Context         context;
    private int             page = 0;
    private boolean         lastPageLoadedSuccessful = false;

    public BlogsScreen(final Context context)
    {
        super(context);

        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.blogs_view, null);
        }

        init();
    }

    public void init()
    {
        list = (ListView) contentView.findViewById(R.id.list);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        adapter = new BlogsAdapter(context, R.layout.post_row_view, new ArrayList<BaseItem>());
        list.setScrollingCacheEnabled(false);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg, View arg1, int arg2,
                    long position)
            {
                Object obj = list.getItemAtPosition((int)position);
                if(obj != null && obj instanceof BaseItem)
                {
                    BaseItem item = (BaseItem)obj;
                    Intent intent = new Intent(LepraDroidApplication.getInstance(), BlogScreen.class);
                    intent.putExtra("groupId", Commons.BLOGS_POSTS_ID.toString());
                    intent.putExtra("id", item.getId().toString());
                    String title = Utils.html2text(item.getHtml());
                    intent.putExtra("title", title.length() > Commons.MAX_BLOG_HEADER_LENGTH ? title.substring(0, Commons.MAX_BLOG_HEADER_LENGTH - 1) + "..." : title);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(intent); 
                }
            }
        });
        
        list.setOnScrollListener(new OnScrollListener()
        {
            public void onScrollStateChanged(AbsListView arg0, int arg1)
            {
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, final int totalItemCount)
            {
                if (    visibleItemCount > 0
                        && firstVisibleItem != 0
                        && firstVisibleItem + visibleItemCount == totalItemCount)
                {
                    if (lastPageLoadedSuccessful && page < ServerWorker.Instance().getPostPagesCount(Commons.BLOGS_POSTS_ID) - 1)
                    {
                        BaseActivity activity = (BaseActivity) context;
                        activity.popAllTasksLikeThis(GetBlogsTask.class);
                        activity.pushNewTask(new TaskWrapper(null, new GetBlogsTask(page + 1), Utils.getString(R.string.Posts_Loading_In_Progress)));
                        adapter.addProgressElement();
                        adapter.notifyDataSetChanged();
                        lastPageLoadedSuccessful = false;
                    }
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    {

    }

    @Override
    public void OnBlogsUpdateBegin(int page)
    {
        if(page == 0)
        {
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            list.setVisibility(View.GONE);
        }
        
        this.page = page;
    }
    
    @Override
    public void OnBlogsUpdate(int page)
    { 
        if(progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
        }

        this.page = page;
        
        updateAdapter();
    }
    
    @Override
    public void OnBlogsUpdateFinished(int page, boolean successful)
    { 
        this.lastPageLoadedSuccessful = successful;
        
        if(successful)
        {
            OnBlogsUpdate(page);
        }
        else
        {
            adapter.removeProgressElement();
            adapter.notifyDataSetChanged();
        }
    }
    
    private void updateAdapter()
    {
        synchronized (this)
        {
            adapter.updateData(ServerWorker.Instance().getPostsById(Commons.BLOGS_POSTS_ID, true));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnExit()
    {
        context = null;
        adapter.clear();
        ListenersWorker.Instance().unregisterListener(this);
    } 
}
