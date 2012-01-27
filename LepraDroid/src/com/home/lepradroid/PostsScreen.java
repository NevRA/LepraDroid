package com.home.lepradroid;


import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.serverworker.ServerWorker;

public class PostsScreen extends BaseView implements PostsUpdateListener
{
    private ListView list;
    public PostsAdapter adapter;
    private PostSourceType type;
    private Context context;
    
    public PostsScreen(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void init(final PostSourceType type, final Context context)
    {
        this.type = type;
        this.context = context;
        
        list = (ListView) findViewById(R.id.list);
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
            adapter = new PostsAdapter(context, R.layout.post_row_view, ServerWorker.Instance().getPostsByType(type));

            list.setAdapter(adapter);
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4)
    {
        
    }
}
