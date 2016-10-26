package com.reactnativecomponent.barcode.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;


public class LinearGradientView extends View {

//  /*  *//**
//     * 中间滑动线的最顶端位置
//     *//*
//    private int slideTop;
//
//    *//**
//     * 中间那条线每次刷新移动的距离
//     *//*
//    private static int SPEEN_DISTANCE = 3;
//
//    *//**
//     * 中间滑动线的最底端位置
//     *//*
//    private int slideBottom;
//
//    *//**
//     * 四个蓝色边角对应的宽度
//     *//*
//    public int CORNER_WIDTH = 3;*/

    /**
     * 框架颜色
     */
    public int frameColor=Color.GREEN;

    /**
     * 扫描线渐变色中间色
     */
    public int frameBaseColor;

    /**
     * 线的厚度
     */
    public int size;
    /**
     * 线的宽度
     */
    public int width;

    Activity activity;

//    /**
//     * 扫描框中的中间线的宽度
//     */
//    private static final int MIDDLE_LINE_WIDTH = 3;


//    public int top,left,right;


//    private Paint paintLine;


    public LinearGradientView(Context context,Activity activity) {
        super(context);
        this.activity=activity;

//        paintLine=new Paint();
    }

    public void setFrameColor(int frameColor) {

        this.frameColor = frameColor;

        this.frameBaseColor = reSetColor(frameColor);
        //渐变色drawable
        int[] mColors = new int[]{Color.TRANSPARENT, frameBaseColor, frameColor, frameColor, frameColor, frameColor, frameColor, frameBaseColor, Color.TRANSPARENT};

        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mColors);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//        drawable.setCornerRadius(15);
//        drawable.setStroke(10,-1);
        setBackground(drawable);
    }

    @Override
    protected void onAttachedToWindow() {
        ViewGroup.LayoutParams params= getLayoutParams();
        if(size>1) {
            params.height = size;
        }
        if(width>1){
            params.width=width;
        }
        setLayoutParams(params);

        super.onAttachedToWindow();

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);



    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
       final ViewGroup.LayoutParams params= getLayoutParams();
        int height = getHeight();
        if (height < 3) {
            params.height = 3;
        } else if (height > 10) {
            params.height = 10;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                LinearGradientView.this.setLayoutParams(params);
            }
        });


        super.onLayout(changed, left, top, right, bottom);

    }




/*    @Override
    protected void onDraw(Canvas canvas) {
        paintLine.setColor(frameColor);


        slideTop += SPEEN_DISTANCE;
        if (slideTop >= slideBottom) {
            slideTop = top + CORNER_WIDTH;
        }
        //自己画
        paintLine.setColor(frameColor);

//                0x8800FF00
        Shader mShader = new LinearGradient(left + CORNER_WIDTH, slideTop, right
                - CORNER_WIDTH, slideTop + MIDDLE_LINE_WIDTH,new int[] {Color.TRANSPARENT,frameBaseColor,frameColor,frameColor,frameColor,frameColor,frameColor,frameBaseColor,Color.TRANSPARENT},null, Shader.TileMode.CLAMP);
//新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。
// 连接这2个点就拉出一条渐变线了，玩过PS的都懂。然后那个数组是渐变的颜色。下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。
// 最后是模式，这里设置的是Clamp渐变
        paintLine.setShader(mShader);
        canvas.drawRect(left + CORNER_WIDTH, slideTop, right
                - CORNER_WIDTH, slideTop + MIDDLE_LINE_WIDTH, paintLine);

    }*/

    /**
     * 中间色颜色换算
     */
    public int reSetColor(int startInt) {

        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endA = startA / 2;// 转化后可以设置 2:半透明度  4:4分之一透明度


        return ((startA + (endA - startA)) << 24)
                | (startR << 16)
                | (startG << 8)
                | (startB);


    }
}
