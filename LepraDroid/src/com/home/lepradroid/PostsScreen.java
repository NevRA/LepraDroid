package com.home.lepradroid;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostsScreen extends BaseView implements PostsUpdateListener
{
    private ListView list;
    private ProgressBar progress;
    public PostsAdapter adapter;
    private PostSourceType type;
    private Context context;
    
    public PostsScreen(final Context context, final PostSourceType type)
    {
        super(context);
        
        this.context = context;
        this.type = type;
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.posts_view, null);
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
                Intent intent = new Intent(LepraDroidApplication.getInstance(), PostScreen.class);
                intent.putExtra("position", position);
                intent.putExtra("type", type.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LepraDroidApplication.getInstance().startActivity(intent); 
            }
        });
    }

    public void OnPostsUpdate(PostSourceType type, boolean haveNewRecords)
    {
    	if(this.type != type) return;
    	
        if(adapter == null || haveNewRecords)
        {
            progress.setVisibility(View.GONE);
            progress.setIndeterminate(false);
            list.setVisibility(View.VISIBLE);
            
            adapter = new PostsAdapter(context, R.layout.post_row_view, ServerWorker.Instance().getPostsByType(type));

            list.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void OnPostsUpdateBegin(PostSourceType type)
    {
        if(this.type != type) return;
        
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
        list.setVisibility(View.GONE);
        
        ServerWorker.Instance().clearPostsByType(type);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    {
        
    }    
}
