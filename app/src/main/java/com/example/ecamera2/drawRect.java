package com.example.ecamera2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**************************************
 surfaceView 另創，畫一個正弦函數

 *不能與camera結合之原因*
 * 此方法為直接創建一個view
 * 應該要為activity或是其他的control才可使用
**************************************/
public class drawRect extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder rectSurfaceHolder;
    //繪圖用Canvas
    private Canvas rectCanvas;
    //子線程標誌
    private boolean rectIsDrawing;
    private int x = 0, y = 0;
    private Paint mPaint;
    private Path mPath;


    public drawRect(Context context) {
        this(context, null);
        initView();
    }

    public drawRect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initView();
    }

    public drawRect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        mPath = new Path();
        //路径起始点(0, 100)
        mPath.moveTo(0, 100);
        initView();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //创建
        rectIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //改變
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //销毁
        rectIsDrawing = false;
    }

    @Override
    public void run() {
        //子线程
        while(rectIsDrawing){
            rectDraw();
            x += 1;
            y = (int)(100 * Math.sin(2 * x * Math.PI / 180) + 400);
            //加入新的座標點
            mPath.lineTo(x, y);
        }
    }

    public void rectDraw(){
        try {
            //get canvas
            rectCanvas = rectSurfaceHolder.lockCanvas();
            //draw rect
            rectCanvas.drawPath(mPath, mPaint);
//            rectCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
        }catch (Exception e){
        }finally {
            if (rectCanvas != null){
                //釋放canvas並提交畫的結果
                rectSurfaceHolder.unlockCanvasAndPost(rectCanvas);
            }
        }
    }
    private void initView(){
        rectSurfaceHolder = getHolder();
        rectSurfaceHolder.addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
    }
}
