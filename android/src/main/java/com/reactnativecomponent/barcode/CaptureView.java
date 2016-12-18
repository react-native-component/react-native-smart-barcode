package com.reactnativecomponent.barcode;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.reactnativecomponent.barcode.camera.CameraManager;
import com.reactnativecomponent.barcode.decoding.CaptureActivityHandler;
import com.reactnativecomponent.barcode.view.LinearGradientView;
import com.reactnativecomponent.barcode.view.ViewfinderView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class CaptureView extends FrameLayout implements TextureView.SurfaceTextureListener {


    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    //private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep=true;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Activity activity;
    private ViewGroup.LayoutParams param;
    private int ScreenWidth, ScreenHeight;
    private TextureView textureView;
    private long beginTime;
    private int height;
    private int width;
    public boolean decodeFlag = true;
    /**
     * 缩放级别拖动条
     */
    private Handler mHandler;
    /**
     * 当前缩放级别  默认为0
     */
    private int mZoom = 0;
    /**
     * react-native 设置属性
     */
    private int cX;//X轴偏移
    private int cY;//Y轴偏移
    private int CORNER_WIDTH = 4;//四个对应角宽度
    private int MIDDLE_LINE_WIDTH = 3;//扫描线宽度
    private int CORNER_COLOR = Color.GREEN;
    private int Min_Frame_Width;//扫描框最小单位
    //框的颜色
    private String Text = "";//扫描框显示的文字
    //扫描框宽
    private int MAX_FRAME_WIDTH;
    //扫描框高
    private int MAX_FRAME_HEIGHT;
    private float density;
    /**
     * s扫码横线的移动时间
     */
    public int scanTime = 1000;
    private long changeTime = 1000;
    private int focusTime = 1000;
    private long sleepTime = 2000;
    public OnEvChangeListener onEvChangeListener;

    private View popupWindowContent;
    private PopupWindow popupWindow;
    private LinearGradientView linearGradientView;
    SurfaceTexture surfaceTexture;
    boolean autoStart = true;//是否自动启动扫描
    String ResultStr="";




   /* private final VerticalSeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(VerticalSeekBar seekBar, int progress,
                                      boolean fromUser) {
            // TODO Auto-generated method stub

            //setZoom(progress);

            mHandler.removeCallbacksAndMessages(progressBar);
            //ZOOM模式下 在结束四秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
            mHandler.postAtTime(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
//                    progressBar.setVisibility(View.GONE);
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
            }, progressBar, SystemClock.uptimeMillis() + 4000);
        }

        @Override
        public void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar) {

        }

        @Override
        public void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar) {

        }


    };*/


    public CaptureView(Activity activity, Context context) {
        super(context);
        this.activity = activity;
        CameraManager.init(activity.getApplication());
        param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        density = context.getResources().getDisplayMetrics().density;
        Min_Frame_Width = (int) (100 * density + 0.5f);
        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        ScreenWidth = dm.widthPixels;
        ScreenHeight = dm.heightPixels;

        //  x=screenResolution.x;
        // y=screenResolution.y;

        hasSurface = false;
        this.setOnTouchListener(new TouchListener());
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        height = getHeight();
        width = getWidth();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }


    private void initCameraManager() {

        CameraManager.get().x = cX + width;
        CameraManager.get().y = cY + height;
        CameraManager.get().MIN_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MIN_FRAME_HEIGHT = MAX_FRAME_HEIGHT;
        CameraManager.get().MAX_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MAX_FRAME_HEIGHT = MAX_FRAME_HEIGHT;
        CameraManager.get().setFocusTime(focusTime);

    }
/*
    @Override
    public void onViewAdded(View child) {
        if (this.viewfinderView == child) return;
        // remove and readd view to make sure it is in the back.
        // @TODO figure out why there was a z order issue in the first place and fix accordingly.
        if (viewfinderView != null) {
            this.removeView(this.viewfinderView);
            this.addView(this.viewfinderView);
        }
    }*/
    
    /**
     * Activity onResume后调用view的onAttachedToWindow
     */
    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    /**
     * surfaceview 扫码框 声音管理
     */
    private void init() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        textureView = new TextureView(activity);
        textureView.setLayoutParams(param);
        textureView.getLayoutParams().height = ScreenHeight;
        textureView.getLayoutParams().width = ScreenWidth;
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        if (hasSurface) {
            initCamera(surfaceTexture);
        } else {
            textureView.setSurfaceTextureListener(this);

        }
        this.addView(textureView);
        viewfinderView = new ViewfinderView(activity, scanTime, CORNER_COLOR);
        viewfinderView.CORNER_WIDTH = CORNER_WIDTH;
        viewfinderView.ShowText = Text;
        viewfinderView.setLayoutParams(param);
        viewfinderView.getLayoutParams().height = ScreenHeight;
        viewfinderView.getLayoutParams().width = ScreenWidth;
        viewfinderView.setBackgroundColor(getResources().getColor(R.color.transparent));
        viewfinderView.setMIDDLE_LINE_WIDTH(this.MIDDLE_LINE_WIDTH);
        this.addView(viewfinderView);

        linearGradientView = new LinearGradientView(activity, activity);
        linearGradientView.setLayoutParams(param);
        linearGradientView.setFrameColor(CORNER_COLOR);


//        decodeFormats = null;
        characterSet = null;



        vibrate = true;

        setPlayBeep(true);
//        initProgressBar();
//        progressBar = new VerticalSeekBar(activity);
      /*  popupWindowContent = View.inflate(activity, R.layout.seekbar_layout, null);
        progressBar = (VerticalSeekBar) popupWindowContent.findViewById(R.id.verticalSeekBar);
        // 给progressbar准备一个FrameLayout的LayoutParams


        progressBar.setIndeterminate(false);
        progressBar.setThumb(null);*/
//        progressBar.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));


//

      /*
       //渐变色drawable
       int[] mColors=  new int[]{Color.WHITE,Color.BLUE};
        GradientDrawable drawable=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,mColors);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setCornerRadius(15);
        drawable.setStroke(10,-1);
        */

/*        LayerDrawable progressDrawable = (LayerDrawable) progressBar
                .getProgressDrawable();
        Drawable[] outDrawables = new Drawable[progressDrawable
                .getNumberOfLayers()];
        for (int i = 0; i < progressDrawable.getNumberOfLayers(); i++) {
            switch (progressDrawable.getId(i)) {
                case android.R.id.background:// 设置进度条背景
                    outDrawables[i] = getResources().getDrawable(R.drawable.seek_bkg);
                    break;
                case android.R.id.secondaryProgress:// 设置二级进度条
                    outDrawables[i] = getResources().getDrawable(R.drawable.seek);
                    break;
                case android.R.id.progress:// 设置进度条
                    ClipDrawable oidDrawable = (ClipDrawable) progressDrawable
                            .getDrawable(i);
                    Drawable drawable=getResources().getDrawable(R.drawable.seek);
                    ClipDrawable proDrawable = new ClipDrawable(drawable,
                            Gravity.LEFT, ClipDrawable.HORIZONTAL);
                    proDrawable.setLevel(oidDrawable.getLevel());
                    outDrawables[i] = proDrawable;
                    break;
                default:
                    break;
            }
        }
        progressDrawable = new LayerDrawable(outDrawables);
        progressBar.setProgressDrawable(progressDrawable);*/


        //        progressBar.setBackgroundResource(R.drawable.seek_bkg);
//        progressBar.setSecondaryProgress(R.drawable.seek);
//        progressBar.setThumb(getResources().getDrawable(R.drawable.seek_thumb));
//        progressBar.setMinimumHeight(20);


        //获取当前照相机支持的最大缩放级别，值小于0表示不支持缩放。当支持缩放时，加入拖动条。
        int maxZoom = getMaxZoom();
        if (maxZoom > 0) {
//            progressBar.setMax(maxZoom);
//            progressBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        }

    }

    public void setPlayBeep(boolean b) {
        playBeep = b;
        AudioManager audioService = (AudioManager) activity.getSystemService(activity.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
           playBeep = false;
        }
        initBeepSound();
    }


    private void initProgressBar() {
/*        if (progressBar != null) {
            LayoutParams progresslp = new LayoutParams(
                    120,
                    MAX_FRAME_HEIGHT);
            // 设置对其方式为：屏幕居中
            int leftMargin = (width / 2) + cX + MAX_FRAME_WIDTH / 2;
            int topMargin = height / 2 + cY / 2 - MAX_FRAME_HEIGHT;
//        progresslp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
//            progressBar.setLayoutParams(param);

            // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
            popupWindow = new PopupWindow(CaptureView.this);
*//*            popupWindow.setWidth((int) (20 * density));
            popupWindow.setHeight(MAX_FRAME_HEIGHT-CORNER_WIDTH*2);
            popupWindow.setContentView(popupWindowContent);*//*

            popupWindow.setWidth(MAX_FRAME_WIDTH);
            popupWindow.setHeight(30);
            popupWindow.setContentView(linearGradientView);

            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);

        }*/
    }


    public void startScan() {
        if (!hasSurface) {
            viewfinderView.drawLine = true;
            hasSurface = true;
            CameraManager.get().framingRectInPreview = null;
            initCamera(surfaceTexture);

            CameraManager.get().initPreviewCallback();
            CameraManager.get().startPreview();

        }
//        decodeFormats = null;

        handler = new CaptureActivityHandler(this, decodeFormats,
                characterSet);
//        handler.restartPreviewAndDecode();
    }

    public void stopScan() {
        hasSurface = false;
        viewfinderView.drawLine = false;
        if (handler != null) {
            handler.quitSynchronously();
        }
        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();
    }

    public void stopQR() {
        this.decodeFlag = false;
    }

    public void startQR() {
        this.decodeFlag = true;
        startScan();
    }

    /**
     * ondestroy调用,会执行onDetachedFromWindow
     */

    @Override
    protected void onDetachedFromWindow() {
        this.removeView(viewfinderView);
        this.removeView(textureView);

        if (handler != null) {
            handler.quitSynchronously();
        }

        super.onDetachedFromWindow();
    }


    private void initCamera(SurfaceTexture surfaceTexture) {
        try {
            CameraManager.get().openDriver(surfaceTexture);


        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }


    public void handleDecode(Result obj, Bitmap barcode) {

//        viewfinderView.drawResultBitmap(barcode);//画结果图片

        if (obj != null&& this.decodeFlag) {
            playBeepSoundAndVibrate();
            String str = obj.getText();//获得扫码的结果
        /*
        activity.getCapturePackage().mModuleInstance.sendMsgToRn(str); //发送到RN侧*/
            onEvChangeListener.getQRCodeResult(str,obj.getBarcodeFormat()); //观察者模式发送到RN侧
        }
        stopQR();


//        viewfinderView.drawResultBitmap(null);//清除结果图片

        /*
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initCamera(textureView.getHolder());
        if (handler != null) {
            handler.restartPreviewAndDecode();
        }*/

    }


    /**
     * 返回数据
     *
     * @param intent
     * @return
     */
    public String ShowResult(Intent intent) {
        return intent.getData().toString();
    }


    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;


    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        //抖动机身
      /*  if (vibrate) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(activity.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }*/
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    public Activity getActivity() {
        return activity;
    }


    public void setHandler(CaptureActivityHandler handler) {
        this.handler = handler;

    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    public void setFocusTime(int focusTime) {
        this.focusTime = focusTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setcX(int cX) {

        if (width != 0 && ((cX > width / 2 - Min_Frame_Width) || cX < (Min_Frame_Width - width / 2))) {
            if (cX > 0) {
                cX = width / 2 - Min_Frame_Width;
            } else {
                cX = Min_Frame_Width - width / 2;
            }
        }

        this.cX = cX;
        CameraManager.get().x = cX + width;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }
        initProgressBar();
    }

    public void setcY(int cY) {


        if (height != 0 && ((cY > height / 2 - Min_Frame_Width) || cY < (Min_Frame_Width - height / 2))) {
            if (cY > 0) {
                cY = height / 2 - Min_Frame_Width;
            } else {
                cY = Min_Frame_Width - height / 2;
            }
        }

        this.cY = cY;
        CameraManager.get().y = cY + height;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

        initProgressBar();
    }

    public void setMAX_FRAME_WIDTH(int MAX_FRAME_WIDTH) {
        if (width != 0 && MAX_FRAME_WIDTH > width) {
            MAX_FRAME_WIDTH = width;
        }
        this.MAX_FRAME_WIDTH = MAX_FRAME_WIDTH;
        CameraManager.get().MIN_FRAME_WIDTH = this.MAX_FRAME_WIDTH;

        CameraManager.get().MAX_FRAME_WIDTH = this.MAX_FRAME_WIDTH;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

        initProgressBar();
    }

    public void setMAX_FRAME_HEIGHT(int MAX_FRAME_HEIGHT) {

        if (height != 0 && MAX_FRAME_HEIGHT > height) {
            MAX_FRAME_HEIGHT = width;
        }
        this.MAX_FRAME_HEIGHT = MAX_FRAME_HEIGHT;

        CameraManager.get().MIN_FRAME_HEIGHT = this.MAX_FRAME_HEIGHT;

        CameraManager.get().MAX_FRAME_HEIGHT = this.MAX_FRAME_HEIGHT;
        CameraManager.get().framingRect = null;
        if (viewfinderView != null) {
            viewfinderView.invalidate();
        }

        initProgressBar();
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            //对应onresume
            this.surfaceTexture = textureView.getSurfaceTexture();
            startScan();
        } else {
            //对应onpause
            stopScan();
        }

    }

    public void setText(String text) {
        Text = text;
    }

    /**
     * 设置扫描线运动时间
     *
     * @param scanTime
     */
    public void setScanTime(int scanTime) {
        this.scanTime = scanTime;
        if (viewfinderView != null) {
            viewfinderView.scanTime = scanTime;
        }
    }

    public void setCORNER_COLOR(int CORNER_COLOR) {
        this.CORNER_COLOR = CORNER_COLOR;
        if (viewfinderView != null) {
            viewfinderView.frameColor = this.CORNER_COLOR;
            viewfinderView.frameBaseColor = reSetColor(this.CORNER_COLOR);
        }

    }

    /**
     * 设置四个角的颜色
     *
     * @param CORNER_WIDTH
     */
    public void setCORNER_WIDTH(int CORNER_WIDTH) {
        this.CORNER_WIDTH = CORNER_WIDTH;

        if (viewfinderView != null) {
            viewfinderView.setCORNER_WIDTH(this.CORNER_WIDTH);
        }
    }

    public void setMIDDLE_LINE_WIDTH(int MIDDLE_LINE_WIDTH) {
        this.MIDDLE_LINE_WIDTH = MIDDLE_LINE_WIDTH;
        if (viewfinderView != null) {
            viewfinderView.setMIDDLE_LINE_WIDTH(this.MIDDLE_LINE_WIDTH);
        }
    }

    /**
     * 开启闪光灯常亮
     */
    public void OpenFlash(){
        try {
            Camera.Parameters param =CameraManager.get().getCamera().getParameters();

            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            CameraManager.get().getCamera().setParameters(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  /**
     * 关闭闪光灯常亮
     */
    public void CloseFlash(){
        try {
            Camera.Parameters param = CameraManager.get().getCamera().getParameters();

            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);


            CameraManager.get().getCamera().setParameters(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RN调用方法
     * 改变扫描框大小
     *
     * @param WIDTH
     * @param HEIGHT
     */
    public void setCHANGE_WIDTH(final int WIDTH, final int HEIGHT) {
        //属性动画
//        Toast.makeText(activity, "width" + WIDTH, Toast.LENGTH_SHORT).show();
        if (viewfinderView != null) {

//            if(popupWindow.isShowing()){
//                popupWindow.dismiss();
//            }

            int widthScan = (width / 2 - Math.abs(cX)) - CORNER_WIDTH;

            if (widthScan < Min_Frame_Width) {
                widthScan = Min_Frame_Width - CORNER_WIDTH;
            }
            int heightScan = (height / 2 - Math.abs(cY)) - CORNER_WIDTH;

            if (heightScan < Min_Frame_Width) {
                heightScan = Min_Frame_Width - CORNER_WIDTH;
            }

            ObjectAnimator animWidth = ObjectAnimator.ofInt(CaptureView.this, "MAX_FRAME_WIDTH", MAX_FRAME_WIDTH, WIDTH / 2 > widthScan ? widthScan * 2 : WIDTH - CORNER_WIDTH);
            ObjectAnimator animHeight = ObjectAnimator.ofInt(CaptureView.this, "MAX_FRAME_HEIGHT", MAX_FRAME_HEIGHT, HEIGHT / 2 > heightScan ? heightScan * 2 : HEIGHT - CORNER_WIDTH);


            AnimatorSet animSet = new AnimatorSet();
            animSet.play(animWidth).with(animHeight);
            animSet.setDuration(changeTime);

            animSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                    viewfinderView.drawLine = false;
                    stopQR();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
//                    stopScan();


                    int widthScan = (width / 2 - Math.abs(cX)) - CORNER_WIDTH;

                    if (widthScan < Min_Frame_Width) {
                        widthScan = Min_Frame_Width - CORNER_WIDTH;
                    }
                    int heightScan = (height / 2 - Math.abs(cY)) - CORNER_WIDTH;

                    if (heightScan < Min_Frame_Width) {
                        heightScan = Min_Frame_Width - CORNER_WIDTH;
                    }

                    MAX_FRAME_WIDTH = WIDTH / 2 > widthScan ? widthScan * 2 : WIDTH - CORNER_WIDTH;
                    MAX_FRAME_HEIGHT = HEIGHT / 2 > heightScan ? heightScan * 2 : HEIGHT - CORNER_WIDTH;

                    CameraManager.get().MIN_FRAME_WIDTH = MAX_FRAME_WIDTH;
                    CameraManager.get().MIN_FRAME_HEIGHT = MAX_FRAME_HEIGHT;
                    CameraManager.get().MAX_FRAME_WIDTH = MAX_FRAME_WIDTH;
                    CameraManager.get().MAX_FRAME_HEIGHT = MAX_FRAME_HEIGHT;

//                    Log.i("Test", "width:" + width + ",height:" + height);
//                    Log.i("Test", "cX:" + cX + ",cY:" + cY);
//                    Log.i("Test", "MAX_FRAME_WIDTH:" + MAX_FRAME_WIDTH + ",MAX_FRAME_HEIGHT:" + MAX_FRAME_HEIGHT);

                    CameraManager.get().framingRectInPreview = null;
//                    decodeFormats = null;
                    viewfinderView.drawLine = true;
                    surfaceTexture = textureView.getSurfaceTexture();
//                    startScan();
                      startQR();
                    initProgressBar();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    stopScan();
                    CameraManager.get().framingRectInPreview = null;
//                    decodeFormats = null;
                    surfaceTexture = textureView.getSurfaceTexture();
                    startScan();
                    viewfinderView.drawLine = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animSet.start();


        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        Log.i("Test", "height:" + height + "width:" + width);

        CameraManager.init(activity);

        initCameraManager();

        surfaceTexture = surface;


        textureView.setAlpha(1.0f);


        if (autoStart) {
            startScan();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopScan();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    private final class TouchListener implements OnTouchListener {

        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 1;
        private int mode = MODE_ZOOM;// 初始状态

        /**
         * 用于记录拖拉图片移动的坐标位置
         */

        private float startDis;


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_ZOOM;
                    startDis = event.getY();
//                    Log.i("Test", "ACTION_DOWN");


//                    int leftMargin = (width / 2) + cX + MAX_FRAME_WIDTH/2-(int)(25*density);
                    int leftMargin = cX / 2 + MAX_FRAME_WIDTH / 2 - (int) (25 * density);
                    int topMargin = cY / 2 - (int) (25 * density);
/**
 * 显示seekbar
 */
//                    popupWindow.showAtLocation(v, Gravity.CENTER, leftMargin,topMargin);

                    break;
             /*   case MotionEvent.ACTION_POINTER_DOWN:
                    //如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
                    if (seekbar == null) return true;
                    //移除token对象为mZoomSeekBar的延时任务
                    mHandler.removeCallbacksAndMessages(seekbar);
                    seekbar.setVisibility(View.VISIBLE);

                    mode = MODE_ZOOM;
                    *//** 计算两个手指间的距离 *//*
                    startDis = distance(event);
                    break;*/
                case MotionEvent.ACTION_MOVE:

                    /**
                     * 控制设置zoom没16毫秒触发,不到时间不触发
                     */

                    if (mode == MODE_ZOOM) {
                        /* //只有同时触屏两个点的时候才执行
                        if(event.getPointerCount()<2) return true;*/
                        float endDis = startDis - event.getY();// 结束距离


                        int scale = (int) (endDis / (ScreenHeight / getMaxZoom()));

                        if (scale == 0 && endDis < 0) {
                            scale = -1;
                        } else if (scale == 0 && endDis > 0) {
                            scale = 1;
                        }
//                        Log.i("Test", "scale:" + scale);

                        /**
                         * 处理时间
                         */
                        long endTime = System.currentTimeMillis();

                        long time = endTime - beginTime;

                        beginTime = System.currentTimeMillis();

                        if (scale >= 1 || scale <= -1) {
                            int zoom = getZoom() + scale;
                            //zoom不能超出范围
                            if (zoom > getMaxZoom()) zoom = getMaxZoom();
                            if (zoom < 0) zoom = 0;
//                            Log.i("Test", "zoom:" + zoom + ",Time:" + time);
                            setZoom(zoom);
//                            progressBar.setProgress(zoom);
                            //将最后一次的距离设为当前距离
//                            startDis = endDis;
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:

                    /*if(mode!=MODE_ZOOM){
                        //设置聚焦
                        Point point=new Point((int)event.getX(), (int)event.getY());
                        mCameraView.onFocus(point,autoFocusCallback);
                        mFocusImageView.startFocus(point);
                    }else {*/
                    //ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务

					/*mHandler.postAtTime(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
                            if(popupWindow.isShowing()){
                                popupWindow.dismiss();
                            }
						}
					}, progressBar,SystemClock.uptimeMillis()+5000);
//                    }*/
                    break;
            }
            return true;
        }

//        /**
//         * 计算两个手指间的距离
//         */
//        private float distance(MotionEvent event) {
//            float dx = event.getX(1) - event.getX(0);
//            float dy = event.getY(1) - event.getY(0);
//            /** 使用勾股定理返回两点之间的距离 */
//            return (float) Math.sqrt(dx * dx + dy * dy);
//        }

    }

    /**
     * 获取最大缩放级别，最大为40
     *
     * @return
     */

    public int getMaxZoom() {
        if (CameraManager.get().getCamera() != null) {
            Camera.Parameters parameters = CameraManager.get().getCamera().getParameters();
            if (!parameters.isZoomSupported()) return -1;

            return parameters.getMaxZoom() > 40 ? 40 : parameters.getMaxZoom();
        }
        return 40;
    }

    /**
     * 设置相机缩放级别
     *
     * @param zoom
     */

    public void setZoom(int zoom) {

        Camera.Parameters parameters;
        //注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
        //stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
        if (CameraManager.get().getCamera() != null) {
            parameters = CameraManager.get().getCamera().getParameters();
            if (!parameters.isZoomSupported()) return;
            parameters.setZoom(zoom);
            CameraManager.get().getCamera().setParameters(parameters);
            mZoom = zoom;
        }
    }

    public int getZoom() {
        return mZoom;
    }

    public Vector<BarcodeFormat> getDecodeFormats() {
        return decodeFormats;
    }

    public void setDecodeFormats(List<String> decode) {
        decodeFormats=new Vector<BarcodeFormat>();
        for(BarcodeFormat format : BarcodeFormat.values()){
            if(decode.contains(format.toString())){
                decodeFormats.add(format);
            }
        }

    }

    public interface OnEvChangeListener {
        public void getQRCodeResult(String result, BarcodeFormat format);
    }

    public void setOnEvChangeListener(OnEvChangeListener onEvChangeListener) {
        this.onEvChangeListener = onEvChangeListener;
    }



    /**
     * 颜色换算
     */
    public int reSetColor(int startInt) {

        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endA = ((startInt / 2) >> 24) & 0xff;
        return ((startA + (endA - startA)) << 24)
                | (startR << 16)
                | (startG << 8)
                | (startB);


    }
}


