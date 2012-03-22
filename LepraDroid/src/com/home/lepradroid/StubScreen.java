package com.home.lepradroid;

import android.content.Intent;
import android.os.Bundle;
import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetPostTask;
import com.home.lepradroid.utils.LinksCatcher;

import java.util.UUID;


public class StubScreen extends BaseActivity implements PostsUpdateListener
{

    private String url;
    private Integer linkType;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub_view);

        url = getIntent().getExtras().getString("url");
        linkType = getIntent().getExtras().getInt("type");

        if (url != null && linkType != null && linkType == LinksCatcher.LINK_POST)
        {

            new GetPostTask(Commons.OTHER_POSTS_ID, url).execute();

        }
        else
        {
            finish();
        }

    }


    @Override
    public void OnPostsUpdateBegin(UUID id, int page)
    {
    }

    @Override
    public void OnPostsUpdate(UUID id, int page)
    {
    }

    @Override
    public void OnPostsUpdateFinished(UUID id, int page, boolean successful)
    {
        if (!successful)
            finish();

        if (linkType == LinksCatcher.LINK_POST)
        {
            BaseItem post = ServerWorker.Instance().getPostsById(Commons.OTHER_POSTS_ID, false).get(0);
            Intent intent = new Intent(this, PostScreen.class);
            intent.putExtra("groupId", Commons.OTHER_POSTS_ID.toString());
            intent.putExtra("id", post.Id.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            finish();
        }
    }
}
