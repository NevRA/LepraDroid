package com.home.lepradroid;

import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.RateItemTask;

public class PostView extends BaseView
{
    //private Context context;
    private UUID    groupId;
    private UUID    id;
    private Post    post;
    
    public PostView(Context context, UUID groupId, UUID id)
    {
        super(context);
        
        //this.context = context;
        this.groupId = groupId;
        this.id = id;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.post_view, null);
        }

        init();
    }

    private void init()
    {
        post = (Post)ServerWorker.Instance().getPostById(groupId, id);
        if(post == null)
            return; // TODO some message
        
        WebView webView = (WebView) contentView.findViewById(R.id.webview);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        
        Button plus = (Button) contentView.findViewById(R.id.plus);
        Button minus = (Button) contentView.findViewById(R.id.minus);
        
        if(post.MinusVoted)
            minus.setEnabled(false);
        if(post.PlusVoted)
            plus.setEnabled(false);
        
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        webView.loadDataWithBaseURL("", header + post.Html, "text/html", "UTF-8", null );
        
        minus.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateItem(RateValueType.MINUS);
            }
        });
        
        plus.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateItem(RateValueType.PLUS);
            }
        });
    }
    
    private void rateItem(RateValueType type)
    {
        new RateItemTask(groupId, id, RateType.POST, SettingsWorker.Instance().loadVoteWtf(), post.Pid, type).execute();
    }

    @Override
    public void OnExit()
    {
        ListenersWorker.Instance().unregisterListener(this);
    }
}
