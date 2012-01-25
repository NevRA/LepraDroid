package com.home.lepradroid;

import android.os.Bundle;

import com.home.lepradroid.base.BaseActivity;
import com.home.lepradroid.tasks.GetBlogsTask;
import com.home.lepradroid.tasks.TaskWrapper;
import com.home.lepradroid.utils.Utils;

public class BlogsScreen extends BaseActivity
{
     @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        pushNewTask(new TaskWrapper(this, new GetBlogsTask(), Utils.getString(R.string.Blogs_Loading_In_Progress)));
    }
}
