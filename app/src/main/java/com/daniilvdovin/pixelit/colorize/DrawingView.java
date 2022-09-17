package com.daniilvdovin.pixelit.colorize;

import static com.daniilvdovin.pixelit.Data.ScaleSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    public int      width;
    public int      height;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Paint   mBitmapPaint;
    private Paint   rectPaint;
    private Paint   drawText;
    Context context;
    public DrawingView(Context context) {
        super(context);
        this.context=context;
        rectPaint = new Paint();
        rectPaint.setAntiAlias(false);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        drawText = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
        drawText.setTextSize(30);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        Log.e("IMAGE",mBitmap.getHeight()+"x"+mBitmap.getWidth());
        for (int i = 30; i < mBitmap.getWidth(); i += 60) {
            for (int j = 30; j < mBitmap.getHeight(); j += 60) {
                rectPaint.setColor(Color.RED);
                //canvas.drawRect(i-30,j-30,i+60,j+60,rectPaint);
                canvas.drawText("1",i-10,j+10,drawText);
            }
        }
        for (int i = 0; i < mBitmap.getWidth(); i++) {
            if(i%60==0)
                for (int j = 0; j < mBitmap.getHeight(); j++) {
                    mBitmap.setPixel(i, j, Color.GRAY);
                }
        }
        for (int i = 0; i < mBitmap.getHeight(); i++) {
            if (i % 60 == 0)
                for (int j = 0; j < mBitmap.getWidth(); j++) {
                    mBitmap.setPixel(j, i, Color.GRAY);
                }
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    private void touch_start(float x, float y) {


    }

    private void touch_move(float x, float y) {
        if(clamp(x,x-30,x+30) && clamp(y,y-30,y+30))
        {
            float h = x/(x-1);
            x = x*h-h;
            float g = y/(y-1);
            y = y*g-g;
            rectPaint.setColor(Color.RED);
            mCanvas.drawRect(x-30,y-30,x+30,y+30,rectPaint);
        }
    }
    public static boolean clamp(float val, float min, float max) {
        return (val<max && val>min);
    }
    private void touch_up() {

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

}
