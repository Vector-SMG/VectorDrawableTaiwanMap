package com.ivan.vectordrawable_taiwanmap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/*
 * @author liuwei
 * @email 13040839537@163.com
 * create at 2018/8/10
 * description:
 */
public class MyView extends View {
    public static final String TAG="MyView";

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG,"init");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG,"onDraw");
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG,"onMeasure");
    }
}
