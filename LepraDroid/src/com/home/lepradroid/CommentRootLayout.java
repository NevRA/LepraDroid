package com.home.lepradroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CommentRootLayout extends RelativeLayout
{
    private boolean isNew = false;
    
    public CommentRootLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public CommentRootLayout(Context context)
    {
        super(context);
        setWillNotDraw(false);
    }
    
    public void setIsNew(boolean isNew)
    {
        this.isNew = isNew;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(isNew)
        {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setStrokeWidth(2);
            paint.setColor(android.graphics.Color.RED);     
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);

            Point point1_draw = new Point(0, 0);       
            Point point2_draw = new Point(15, 0);   
            Point point3_draw = new Point(0, 15);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(point1_draw.x,point1_draw.y);
            path.lineTo(point2_draw.x,point2_draw.y);
            path.lineTo(point3_draw.x,point3_draw.y);
            path.lineTo(point1_draw.x,point1_draw.y);
            path.close();

            canvas.drawPath(path, paint);
        }

        super.onDraw(canvas);
    }
}
