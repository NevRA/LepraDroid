package com.home.lepradroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.lepradroid.base.BaseView;
import com.home.lepradroid.interfaces.AuthorUpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Author;
import com.home.lepradroid.utils.ImageLoader;

public class AuthorView extends BaseView implements AuthorUpdateListener
{
    private String              userName;
    private RelativeLayout      contentLayout;
    private TextView            name;
    private TextView            ego;
    private TextView            rating;
    private ImageView           userPic;
    private ProgressBar         progress;
    private ImageLoader         imageLoader;
    
    public AuthorView(Context context, String userName)
    {
        super(context);
        
        this.userName = userName;
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            contentView = inflater.inflate(R.layout.author_view, null);
        }
        
        init();
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    private void init()
    {
        imageLoader=new ImageLoader(LepraDroidApplication.getInstance());
        
        contentLayout = (RelativeLayout) contentView.findViewById(R.id.content);
        name = (TextView) contentView.findViewById(R.id.name);
        ego = (TextView) contentView.findViewById(R.id.userego);
        rating = (TextView) contentView.findViewById(R.id.rating);
        userPic = (ImageView) contentView.findViewById(R.id.image);
        progress = (ProgressBar) contentView.findViewById(R.id.progress);
    }

    @Override
    public void OnExit()
    {
        ListenersWorker.Instance().unregisterListener(this);
    }

    @Override
    public void OnAuthorUpdate(String userName, Author data)
    {
        if(!this.userName.equals(userName)) return;
        
        progress.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        
        if(data == null) return;
        
        name.setText(data.Name);
        ego.setText(data.Ego);
        rating.setText(data.Rating.toString());
        //rating.setTextColor(data.Rating > 0 ? Color.GREEN : Color.RED);
        imageLoader.DisplayImage(data.ImageUrl, userPic, R.drawable.ic_user);
    }

    @Override
    public void OnAuthorUpdateBegin(String name)
    {
        progress.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }
}
