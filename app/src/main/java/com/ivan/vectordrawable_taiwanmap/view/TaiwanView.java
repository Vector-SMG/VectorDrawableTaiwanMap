package com.ivan.vectordrawable_taiwanmap.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.ivan.vectordrawable_taiwanmap.R;
import com.ivan.vectordrawable_taiwanmap.bean.PathBean;
import com.ivan.vectordrawable_taiwanmap.utils.PathParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * @author liuwei
 * @email 13040839537@163.com
 * create at 2018/8/10
 * description:可交互的台湾地图
 */
public class TaiwanView extends View {
    public static final String TAG = "TaiwanView";
    //最小宽高
    private final int minHeight;
    private final int minWidth;
    //初始化一个画笔
    Paint mPaint;
    //上下文
    private Context context;
    //解析出的路径集合
    private List<PathBean> pathBeanList = new ArrayList<>();
    //动作检测
    private GestureDetectorCompat gestureDetectorCompat;
    //被点击的path
    private PathBean mSelectPath;
    //缩放值
    private float scaleX, scaleY;
    //svg的边界
    RectF totalRectF;
    //测量的view的宽度，高度
    private int measureWidth;
    private int measureHeight;
    //svg的边界Rect是否绘制完成的标识
    private boolean isComputeTotalRectComplete = false;

    public TaiwanView(Context context) {
        this(context, null);
    }

    public TaiwanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minHeight = context.getResources().getDimensionPixelSize(R.dimen.map_min_height);
        minWidth = context.getResources().getDimensionPixelSize(R.dimen.map_min_width);

        gestureDetectorCompat = new GestureDetectorCompat(this.context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                computeSelectPath((int) (e.getX() / scaleX), (int) (e.getY() / scaleY));
                return super.onDown(e);
            }
        });
        loadSvgFileThread.start();
    }

    private void computeSelectPath(int x, int y) {
        for (PathBean pathBean : pathBeanList) {
            if (pathBean.isTouch(x, y)) {
                mSelectPath = pathBean;
                invalidate();
                break;
            }
        }
    }

    //加载svg文件线程
    private Thread loadSvgFileThread = new Thread(new Runnable() {
        @Override
        public void run() {
            InputStream inputStream = context.getResources().openRawResource(R.raw.taiwan);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);
                Element element = document.getDocumentElement();
                NodeList nodeList = element.getElementsByTagName("path");
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element rootElement = (Element) nodeList.item(i);
                    String pathData = rootElement.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);
                    RectF tmp = new RectF();
                    path.computeBounds(tmp, true);
                    left = left == -1 ? tmp.left : Math.min(left, tmp.left);
                    right = right == -1 ? tmp.right : Math.max(right, tmp.right);
                    top = top == -1 ? tmp.top : Math.min(top, tmp.top);
                    bottom = bottom == -1 ? tmp.bottom : Math.max(bottom, tmp.bottom);
                    PathBean pathBean = new PathBean(path);
                    pathBeanList.add(pathBean);
                }
                totalRectF = new RectF(left, top, right, bottom);
                isComputeTotalRectComplete = true;
                Log.e(TAG, "loadSvgFileThread " + totalRectF.left + " " + totalRectF.right + " " + totalRectF.top + " " + totalRectF.bottom);
                handler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isComputeTotalRectComplete) {
            canvas.save();
            scaleX = measureWidth / totalRectF.width();
            scaleY = measureHeight / totalRectF.height();
            canvas.scale(scaleX, scaleY);
            for (PathBean pathBean : pathBeanList) {
                if (pathBean != mSelectPath) {
                    pathBean.draw(canvas, mPaint, false);
                }
            }
            if (mSelectPath != null) {
                mSelectPath.draw(canvas, mPaint, true);
            }
            canvas.restore();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        measureWidth = 0;
        measureHeight = 0;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                measureWidth = width > minWidth ? width : minWidth;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                measureWidth = minWidth;
                break;
        }
        int computeHeight = minHeight * measureWidth / minWidth;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                measureHeight = height;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                measureHeight = minHeight > computeHeight ? minHeight : computeHeight;
                break;
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY));
    }
}
