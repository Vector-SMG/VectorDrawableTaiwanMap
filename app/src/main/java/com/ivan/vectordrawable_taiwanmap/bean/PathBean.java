package com.ivan.vectordrawable_taiwanmap.bean;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/*
 * @author liuwei
 * @email 13040839537@163.com
 * create at 2018/8/10
 * description:路径数据类
 */
public class PathBean {

    private Path path;


    public PathBean(Path path) {
        this.path = path;
    }


    public RectF getRectF(){
        RectF rectF=new RectF();
        path.computeBounds(rectF,true);
        return rectF;
    }


    public void draw(Canvas canvas, Paint mPaint, boolean isSelected) {
        //如果选中
        if (isSelected) {
            //绘制内容
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#239BD7"));
            canvas.drawPath(path, mPaint);

            //绘制边框
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
            mPaint.setColor(Color.parseColor("#239BD7"));
            canvas.drawPath(path, mPaint);
        } else {
            //绘制内容
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#F5F5F5"));
            canvas.drawPath(path, mPaint);

            //绘制边框
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
            mPaint.setColor(Color.parseColor("#C3C3C3"));
            canvas.drawPath(path, mPaint);
        }
    }

    public boolean isTouch(int x, int y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains(x, y);
    }
}
