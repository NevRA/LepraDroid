package com.home.lepradroid;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.home.lepradroid.objects.Comment;

class CommentsAdapter extends ArrayAdapter<Comment>
{
    private ArrayList<Comment> comments = new ArrayList<Comment>();
            
    public CommentsAdapter(Context context, int textViewResourceId,
            ArrayList<Comment> comments)
    {
        super(context, textViewResourceId, comments);
        this.comments = comments;
    }

    public int getCount() 
    {
        return comments.size();
    }
    
    public Comment getItem(int position) 
    {
        return comments.get(position);
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final Comment comment = getItem(position);
        
        LayoutInflater aInflater=LayoutInflater.from(getContext());

        View view = aInflater.inflate(R.layout.comments_row_view, parent, false);
        
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(comment.Text);
        
        TextView author = (TextView)view.findViewById(R.id.author);
        author.setText(Html.fromHtml(comment.Signature));
        
        TextView rating = (TextView)view.findViewById(R.id.rating);
        rating.setText(comment.Rating.toString());

        return view;
    }
}
