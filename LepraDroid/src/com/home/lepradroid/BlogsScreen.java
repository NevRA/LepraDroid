package com.home.lepradroid;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.BlogsUpdateListener;
import com.home.lepradroid.interfaces.ImagesUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.BaseItem;
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
        adapter = new BlogsAdapter(context, R.layout.post_row_view, new ArrayList<BaseItem>());
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
                    intent.putExtra("id", item.Id.toString());
                    intent.putExtra("title", item.Text.length() > Commons.MAX_BLOG_HEADER_LENGTH ? item.Text.substring(0, Commons.MAX_BLOG_HEADER_LENGTH - 1) + "..." : item.Text);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(intent); 
                }
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
        
        updateAdapter();
    }
    
    @Override
    public void OnBlogsUpdate(boolean haveNewRecords)
    { 
        if(progress.getVisibility() == View.VISIBLE)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
        }
        
        updateAdapter();
    }
    
    private void updateAdapter()
    {
        adapter.updateData(ServerWorker.Instance().getPostsById(Commons.BLOGS_POSTS_ID, true));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnImagesUpdate(UUID groupId)
    {
        if(Commons.BLOGS_POSTS_ID != groupId) return;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnExit()
    {
        adapter.clear();
        ListenersWorker.Instance().unregisterListener(this);
    } 
}
