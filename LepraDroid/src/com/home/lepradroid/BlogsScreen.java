package com.home.lepradroid;

import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.BlogsUpdateListener;
import com.home.lepradroid.interfaces.ImagesUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;

public class BlogsScreen extends BaseView implements BlogsUpdateListener, ImagesUpdateListener
{
    private ListView list;
    private ProgressBar progress;
    public BlogsAdapter adapter;
    private Context context;

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
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            public void onItemClick(AdapterView<?> arg, View arg1, int arg2,
                    long position)
            {
                
            }
        });
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    {

    }

    @Override
    public void OnBlogsUpdateBegin()
    {
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        
        ServerWorker.Instance().clearPostsById(Commons.BLOGS_POSTS_ID);
    }
    
    @Override
    public void OnBlogsUpdate(boolean haveNewRecords)
    { 
        if(adapter == null || haveNewRecords)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
            
            adapter = new BlogsAdapter(context, R.layout.post_row_view, ServerWorker.Instance().getPostsById(Commons.BLOGS_POSTS_ID));
    
            list.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void OnImagesUpdate(UUID groupId)
    {
        if(Commons.BLOGS_POSTS_ID != groupId) return;
        if(adapter != null)
            adapter.notifyDataSetChanged();
    } 
}
