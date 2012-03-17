package com.home.lepradroid;

import java.util.UUID;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.RateType;
import com.home.lepradroid.commons.Commons.RateValueType;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.RateItemTask;
import com.home.lepradroid.utils.Utils;

public class PostView extends BaseView implements ItemRateUpdateListener
{
    private Context context;
    private UUID    groupId;
    private UUID    id;
    private Post    post;
    private Button  plus;
    private Button  minus;
    
    public PostView(Context context, UUID groupId, UUID id)
    {
        super(context);
        
        this.context = context;
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
        
        LinearLayout buttons = (LinearLayout) contentView.findViewById(R.id.buttons);
        if(     groupId.equals(Commons.INBOX_POSTS_ID) ||
                post.voteDisabled ||
                post.Author.equalsIgnoreCase(SettingsWorker.Instance().loadUserName()))
        {
            buttons.setVisibility(View.GONE);
        }
        
        plus = (Button) contentView.findViewById(R.id.plus);
        minus = (Button) contentView.findViewById(R.id.minus);
        
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
                minus.setEnabled(false);
                plus.setEnabled(true);
            }
        });
        
        plus.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateItem(RateValueType.PLUS);
                minus.setEnabled(true);
                plus.setEnabled(false);
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
        context = null;
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnItemRateUpdate(UUID groupId, UUID postId, int newRating, boolean successful)
    {
       if(!this.groupId.equals(groupId) || !this.id.equals(postId)) return;
       
       if(successful)
       {
           if(     groupId.equals(Commons.FAVORITE_POSTS_ID) ||
                   groupId.equals(Commons.MYSTUFF_POSTS_ID))
               Toast.makeText(context, Utils.getString(R.string.Rated_Item_Without_New_Rating), Toast.LENGTH_LONG).show();
           else
               Toast.makeText(context, Utils.getString(R.string.Rated_Item) + " " + Integer.toString(newRating), Toast.LENGTH_LONG).show();
       }
       
       if(post.MinusVoted) 
           minus.setEnabled(false);
       else
           minus.setEnabled(true);
       
       if(post.PlusVoted) 
           plus.setEnabled(false);
       else
           plus.setEnabled(true);
    }

    @Override
    public void OnItemRateUpdate(String userId, boolean successful)
    {
    }
}
