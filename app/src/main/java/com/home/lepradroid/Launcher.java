package com.home.lepradroid;

import android.app.ActivityManager;
import android.content.Context;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class Launcher extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            if (needStartApp())
            {
                Intent intent = new Intent(Launcher.this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        finally
        {
            finish();
        }
    }

    private boolean needStartApp()
    {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);

        if (!tasksInfo.isEmpty())
        {
            final String ourAppPackageName = getPackageName();
            ActivityManager.RunningTaskInfo taskInfo;
            for (ActivityManager.RunningTaskInfo aTasksInfo : tasksInfo)
            {
                taskInfo = aTasksInfo;
                if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName()))
                {
                    return taskInfo.numActivities == 1;
                }
            }
        }

        return true;
    }
}
