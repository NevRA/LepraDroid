package com.home.lepradroid.utils;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.home.lepradroid.LepraDroidApplication;
import com.home.lepradroid.StubScreen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LinksCatcher extends WebViewClient {

    public static final int LINK_POST    = 1;
    public static final int LINK_COMMENT = 2;
    public static final int LINK_PROFILE = 3;

    private static final String PATTERN_POST    = "http://.*leprosorium.ru/comments/\\d{7}(#new)?";
    private static final String PATTERN_COMMENT = "http://.*leprosorium.ru/comments/\\d{7}#\\d{8}";
    private static final String PATTERN_PROFILE = "http://leprosorium.ru/users/.*";

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        Pattern patternPost    = Pattern.compile(PATTERN_POST);
        Pattern patternComment = Pattern.compile(PATTERN_COMMENT);
        Pattern patternProfile = Pattern.compile(PATTERN_PROFILE);

        int linkType = -1;

        if (patternPost.matcher(url).matches())
            linkType = LINK_POST;

        if (patternComment.matcher(url).matches())
            //linkType = LINK_COMMENT;  //  not implemented yet
            linkType = -1;

        if (patternProfile.matcher(url).matches())
            //linkType = LINK_PROFILE;  //  not implemented yet
            linkType = -1;


        if (linkType != -1){
            Intent stubIntent = new Intent(LepraDroidApplication.getInstance(), StubScreen.class);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            stubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stubIntent.putExtra("url", url);
            stubIntent.putExtra("type", linkType);
            LepraDroidApplication.getInstance().startActivity(stubIntent);

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LepraDroidApplication.getInstance().startActivity(intent);
        }
        return true;
    }

}
