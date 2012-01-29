package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.home.lepradroid.objects.BaseItem;
import com.home.lepradroid.objects.Blog;
import com.home.lepradroid.utils.Utils;

class BlogsAdapter extends ArrayAdapter<BaseItem>
{
    private ArrayList<BaseItem> posts = new ArrayList<BaseItem>();
            
    public BlogsAdapter(Context context, int textViewResourceId,
            ArrayList<BaseItem> posts)
    {
        super(context, textViewResourceId, posts);
        this.posts = posts;
    }

    public int getCount() 
    {
        return posts.size();
    }
    
    public BaseItem getItem(int position) 
    {
        return posts.get(position);
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final Blog blog = (Blog)getItem(position);
        
        LayoutInflater aInflater=LayoutInflater.from(getContext());

        View view = aInflater.inflate(R.layout.blog_row_view, parent, false);
        
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(blog.Text);
        
        TextView author = (TextView)view.findViewById(R.id.author);
        author.setText(Html.fromHtml(blog.Signature));
        
        TextView stat = (TextView)view.findViewById(R.id.stat);
        stat.setText(Html.fromHtml(blog.Stat));
        
        ImageView image = (ImageView)view.findViewById(R.id.image);
        
        if(!TextUtils.isEmpty(blog.ImageUrl))
        {
            image.setVisibility(View.VISIBLE);
            
            if(blog.image != null)
            {
                image.setImageDrawable(Utils.getImageFromByteArray(blog.image));
            }
        }

        return view;
    }
}
