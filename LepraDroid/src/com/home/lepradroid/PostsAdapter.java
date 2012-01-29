package com.home.lepradroid;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Post;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class PostsAdapter extends ArrayAdapter<BaseItem>
{
    private ArrayList<BaseItem> posts = new ArrayList<BaseItem>();
    private ReentrantReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
    private final Lock read  = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();
            
    public PostsAdapter(Context context, int textViewResourceId,
            ArrayList<BaseItem> posts)
    {
        super(context, textViewResourceId, posts);
        this.posts = posts;
    }

    public int getCount() 
    {
        read.lock();
        try
        {
            return posts.size();
        }
        finally
        {
            read.unlock();
        } 
    }
    
    public BaseItem getItem(int position) 
    {
        read.lock();
        try
        {
            return posts.get(position);
        }
        finally
        {
            read.unlock();
        }
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    public void updateContent(ArrayList<BaseItem> newPosts)
    {
        write.lock();
        try
        {
            this.posts = newPosts;
        }
        finally
        {
            write.unlock();
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        read.lock();
        final Post post;
        try
        {
            post  = (Post)getItem(position);
        }
        finally
        {
            read.unlock();
        }

        LayoutInflater aInflater=LayoutInflater.from(getContext());

        View view = aInflater.inflate(R.layout.post_row_view, parent, false);
        
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(post.Text);
        
        TextView author = (TextView)view.findViewById(R.id.author);
        author.setText(Html.fromHtml(post.Signature));
        
        TextView comments = (TextView)view.findViewById(R.id.comments);
        comments.setText(Html.fromHtml(post.Comments));
        
        TextView rating = (TextView)view.findViewById(R.id.rating);
        rating.setText(post.Rating.toString());
        
        ImageView image = (ImageView)view.findViewById(R.id.image);
        
        if(!TextUtils.isEmpty(post.ImageUrl))
        {
            image.setVisibility(View.VISIBLE);
            
            if(post.dw != null)
            {
                image.setImageDrawable(post.dw);
            }
        }

        return view;
    }
}
