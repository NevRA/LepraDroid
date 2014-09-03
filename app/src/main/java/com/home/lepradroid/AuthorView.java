package com.home.lepradroid;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.interfaces.AddedInboxUpdateListener;
import com.home.lepradroid.interfaces.AuthorUpdateListener;
import com.home.lepradroid.interfaces.ItemRateUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.settings.SettingsWorker;
import com.home.lepradroid.tasks.RateItemTask;
import com.home.lepradroid.utils.ImageLoader;
import com.home.lepradroid.utils.LinksCatcher;
import com.home.lepradroid.utils.Utils;

import java.util.UUID;

public class AuthorView extends BaseView implements AuthorUpdateListener,
        ItemRateUpdateListener, AddedInboxUpdateListener
{
    private Context context;
    private String userName;
    private String userId;
    private RelativeLayout contentLayout;
    private TextView name;
    private TextView ego;
    private WebView userStory;
    private LinearLayout userStoryLayout;
    private TextView rating;
    private TextView line;
    private ImageView userPic;
    private ProgressBar progress;
    private ImageLoader imageLoader;
    private LinearLayout buttonsLayout;
    private Button plus;
    private Button minus;

    public AuthorView(Context context, String userName)
    {
        super(context);
        this.context = context;
        this.userName = userName;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.author_view, null);
            init();
        }
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    private void init()
    {
        imageLoader = new ImageLoader(LepraDroidApplication.getInstance());

        contentLayout = (RelativeLayout) contentView.findViewById(R.id.content);
        name = (TextView) contentView.findViewById(R.id.name);
        ego = (TextView) contentView.findViewById(R.id.userego);
        rating = (TextView) contentView.findViewById(R.id.rating);
        line = (TextView) contentView.findViewById(R.id.line);
        userPic = (ImageView) contentView.findViewById(R.id.image);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
        userStory = (WebView) contentView.findViewById(R.id.userstory);
        userStoryLayout = (LinearLayout) contentView.findViewById(R.id.userstory_layout);
        buttonsLayout = (LinearLayout) contentView.findViewById(R.id.buttons);
        plus = (Button) contentView.findViewById(R.id.plus);
        minus = (Button) contentView.findViewById(R.id.minus);
        
        Utils.setTextViewFontSize(name);
        Utils.setTextViewFontSize(ego);
        Utils.setTextViewFontSize(line);
        Utils.setTextViewFontSize(rating);
        Utils.setWebViewFontSize(userStory);
    }

    @Override
    public void OnExit()
    {
        context = null;
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnAuthorUpdate(String userName, final Author data)
    {
        if (!this.userName.equals(userName))
            return;

        progress.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);

        if (!SettingsWorker.Instance().loadUserName().equals(userName))
            buttonsLayout.setVisibility(View.VISIBLE);

        if (data == null)
            return;
        
        userId = data.getId();
        name.setText(Html.fromHtml("<b>" + data.getName() + " (" + data.getUserName() + ")" + "</b>"));
        ego.setText(data.getEgo());
        rating.setText(Integer.toString(data.getRating()));
        imageLoader.DisplayImage(data.getImageUrl(), userPic, R.drawable.ic_user);

        if(!TextUtils.isEmpty(data.getUserStory()))
        {
            userStoryLayout.setVisibility(View.VISIBLE);
            userStory.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            userStory.setWebViewClient(LinksCatcher.Instance());
            WebSettings webSettings = userStory.getSettings();
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webSettings.setDefaultFontSize(Commons.WEBVIEW_DEFAULT_FONT_SIZE);
            webSettings.setNeedInitialFocus(false);
            userStory.loadDataWithBaseURL("", "<body style=\"margin: 0; padding: 0\"><center>" + data.getUserStory() + "</center></body>", "text/html", "UTF-8", null);
            Utils.setWebViewFontSize(userStory);
        }

        minus.setEnabled(true);
        plus.setEnabled(true);

        if (data.isMinusVoted())
            minus.setEnabled(false);
        if (data.isPlusVoted())
            plus.setEnabled(false);

        minus.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateItem(Commons.RateValueType.MINUS, data.getId());
                minus.setEnabled(false);
                plus.setEnabled(true);
            }
        });

        plus.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateItem(Commons.RateValueType.PLUS, data.getId());
                minus.setEnabled(true);
                plus.setEnabled(false);
            }
        });

    }

    private void rateItem(Commons.RateValueType type, String id)
    {
        new RateItemTask(id, type).execute();
    }
    
    @Override
    public void OnAuthorRateUpdate(String userId, boolean successful)
    {
        if (    userName.equals(SettingsWorker.Instance().loadUserName()) ||
                !this.userId.equals(userId))
            return;
        
        if (successful)
        {
            Toast.makeText(
                    context,
                    Utils.getString(R.string.Rated_Item_Without_New_Rating), Toast.LENGTH_LONG)
                    .show();
        }
        
        Author author = ServerWorker.Instance().getAuthorByName(userName);
        if (author.isMinusVoted())
            minus.setEnabled(false);
        else
            minus.setEnabled(true);

        if (author.isPlusVoted())
            plus.setEnabled(false);
        else
            plus.setEnabled(true);
    }

    @Override
    public void OnPostRateUpdate(UUID postId, int newRating,
            boolean successful)
    {
    }

    @Override
    public void OnAuthorUpdateBegin(String userName)
    {
        if (!this.userName.equals(userName))
            return;

        progress.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
    }

    @Override
    public void OnCommentRateUpdate(UUID postId, UUID commentId,
            boolean successful)
    { 
    }

    @Override
    public void OnAddedInboxUpdate(String userName, boolean successful)
    {
        if(!this.userName.equals(userName)) return;

        if(successful)
            Toast.makeText(context, Utils.getString(R.string.Created_Inbox), Toast.LENGTH_LONG).show();
    }
}