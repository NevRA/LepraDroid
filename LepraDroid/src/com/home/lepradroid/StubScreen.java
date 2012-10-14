package com.home.lepradroid;

import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostUpdateListener;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.LinksCatcher;

public class StubScreen extends BaseActivity implements PostUpdateListener
{
    private Integer linkType;
    private UUID    groupId;
    private UUID    postId = UUID.randomUUID();
    private String  commentId;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub_view);

        String url = getIntent().getExtras().getString("url");
        linkType = getIntent().getExtras().getInt("type");

        if (    url != null &&
                linkType != null &&
                (linkType == LinksCatcher.LINK_POST || linkType == LinksCatcher.LINK_COMMENT || linkType == LinksCatcher.LINK_INBOX))
        {
            if(linkType == LinksCatcher.LINK_COMMENT)
                commentId = getIntent().getExtras().getString("commentId");

            groupId = linkType == LinksCatcher.LINK_INBOX ? Commons.INBOX_POSTS_ID : Commons.OTHER_POSTS_ID;

            pushNewTask(new TaskWrapper(null, new GetPostTask(groupId, postId, url), ""));
        }
        else
        {
            finish();
        }
    }

    @Override
    public void OnPostUpdateFinished(UUID postId, boolean successful)
    {
        if (!successful)
            finish();

        if (    linkType == LinksCatcher.LINK_POST ||
                linkType == LinksCatcher.LINK_COMMENT ||
                linkType == LinksCatcher.LINK_INBOX)
        {
            BaseItem post = ServerWorker.Instance().getPostById(postId);
            Intent intent = new Intent(this, PostScreen.class);
            intent.putExtra("groupId", groupId.toString());
            intent.putExtra("id", post.getId().toString());
            if(linkType == LinksCatcher.LINK_COMMENT)
                intent.putExtra("commentId", commentId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            finish();
        }
    }
}