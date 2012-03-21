package com.home.lepradroid.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.PostScreen;
import com.home.lepradroid.R;
import com.home.lepradroid.StubScreen;
import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.tasks.GetAuthorTask;
import com.home.lepradroid.tasks.GetPostTask;
import com.home.lepradroid.tasks.GetPostsTask;
import com.home.lepradroid.tasks.TaskWrapper;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: GGobozov
 * Date: 21.03.12
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public class LinksCatcher extends WebViewClient {

    private static final String POST_PATTERN = "http://.*leprosorium.ru/comments/\\d{7}(#new)?";
    private static final String COMMENT_PATTERN = "http://.*leprosorium.ru/comments/\\d{7}#\\d{8}";

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        Pattern pattern = Pattern.compile(POST_PATTERN);
        Matcher postMatcher = pattern.matcher(url);

        if (postMatcher.matches()) {
                Throwable res = null;

                try {

                    Intent stubIntent = new Intent(LepraDroidApplication.getInstance(), StubScreen.class);
                    stubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(stubIntent);


                    res = new GetPostTask(Commons.OTHER_POSTS_ID, url).execute().get();
                    if(res != null)
                        throw res;

                    BaseItem post = ServerWorker.Instance().getPostsById(Commons.OTHER_POSTS_ID, false).get(0);
                    Intent intent = new Intent(LepraDroidApplication.getInstance(), PostScreen.class);
                    intent.putExtra("groupId", Commons.OTHER_POSTS_ID.toString());
                    intent.putExtra("id", post.Id.toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LepraDroidApplication.getInstance().startActivity(intent);

                } catch (Throwable e){
                    e.printStackTrace();
                }


        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LepraDroidApplication.getInstance().startActivity(intent);
        }
        return true;
    }

}
