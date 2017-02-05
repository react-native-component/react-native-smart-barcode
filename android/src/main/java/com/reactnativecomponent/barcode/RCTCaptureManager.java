package com.reactnativecomponent.barcode;


import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class RCTCaptureManager extends ViewGroupManager<CaptureView> {
    private static final String REACT_CLASS = "CaptureView";//要与类名一致
    public static final int CHANGE_SHOW = 0;//用来标记方法的下标
   Activity activity;
    CaptureView cap;
    private float density;


//    public RCTCaptureManager(Activity activity) {
//        this.activity = activity;
//        density = activity.getResources().getDisplayMetrics().density;
//    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public CaptureView createViewInstance(ThemedReactContext context) {
        Activity activity = context.getCurrentActivity();
        density = activity.getResources().getDisplayMetrics().density;
        cap = new CaptureView(activity, context);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//        cap.setLayoutParams(params);

        return cap;
    }
    @ReactProp(name = "barCodeTypes")
    public void setbarCodeTypes(CaptureView view, ReadableArray barCodeTypes) {

        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        view.setDecodeFormats(result);

    }
    @ReactProp(name = "scannerRectLeft", defaultInt = 0)
    public void setCX(CaptureView view, int cX) {
        view.setcX((int) (cX* density + 0.5f));
    }

    @ReactProp(name = "scannerRectTop", defaultInt = 0)
    public void setCY(CaptureView view, int cY) {
        view.setcY((int)(cY* density + 0.5f));
    }

    @ReactProp(name = "scannerRectWidth", defaultInt = 255)
    public void setMAX_FRAME_WIDTH(CaptureView view, int FRAME_WIDTH) {
        view.setMAX_FRAME_WIDTH((int) (FRAME_WIDTH * density + 0.5f));
    }

    @ReactProp(name = "scannerRectHeight", defaultInt = 255)
    public void setMAX_FRAME_HEIGHT(CaptureView view, int FRAME_HEIGHT) {
        view.setMAX_FRAME_HEIGHT((int) (FRAME_HEIGHT * density + 0.5f));
    }

/*    @ReactProp(name = "text")
    public void setText(CaptureView view, String text) {
        view.setText(text);
    }*/


  /*  @ReactProp(name = "scannerRectCornerWidth", defaultInt = 4)
    public void setCORNER_WIDTH(CaptureView view, int CORNER_WIDTH) {
        if(CORNER_WIDTH<4){
            CORNER_WIDTH=4;
        }
        view.setCORNER_WIDTH(CORNER_WIDTH);
    }*/

   /* @ReactProp(name = "scannerLineWidth", defaultInt = 3)
    public void setMIDDLE_LINE_WIDTH(CaptureView view, int MIDDLE_LINE_WIDTH) {
        if(MIDDLE_LINE_WIDTH<3){
            MIDDLE_LINE_WIDTH=3;
        }
        view.setMIDDLE_LINE_WIDTH(MIDDLE_LINE_WIDTH);
    }*/

    //扫描线移动一圈时间
    @ReactProp(name = "scannerLineInterval", defaultInt = 1000)
    public void setTime(CaptureView view, int time) {
        view.setScanTime(time);
    }

   /* //扫描框尺寸动画持续时间
    @ReactProp(name = "changeTime", defaultInt = 1000)
    public void setChangeTime(CaptureView view, int time) {
        view.setChangeTime(time);
    }

    //camera聚集时间
    @ReactProp(name = "focusTime", defaultInt = 1000)
    public void setfocusTime(CaptureView view, int time) {
        view.setFocusTime(time);
    }

    @ReactProp(name = "autoStart", defaultBoolean = true)
    public void setAutoStart(CaptureView view, boolean start) {
        view.setAutoStart(start);
    }*/

    @ReactProp(name = "scannerRectCornerColor")
    public void setCORNER_COLOR(CaptureView view, String color) {
        if (color != null && !color.isEmpty()) {
            view.setCORNER_COLOR(Color.parseColor(color));//转换成16进制
        }
    }

  /*  //扫码成功提示音
    @ReactProp(name = "playBeep",defaultBoolean = true)
    public void setPlayBeep(CaptureView view, boolean isBeep) {
            view.setPlayBeep(isBeep);
    }
*/

    @Override
    public
    @Nullable
    Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "change",
                CHANGE_SHOW);//js处发送的方法名字
    }

    @Override
    public void receiveCommand(CaptureView root, int commandId, @Nullable ReadableArray config) {
       // super.receiveCommand(root, commandId, config);
        if (commandId == CHANGE_SHOW) {
            this.changeWidthHeight(config.getMap(0));
        }
    }


    @ReactMethod
    public void changeWidthHeight(final ReadableMap config) {
//        Log.i("Test", "changeWidthHeight");
        if (cap != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    int width = config.getInt("FRAME_WIDTH");
                    int height = config.getInt("FRAME_HEIGHT");
                    cap.setCHANGE_WIDTH((int)(width* density + 0.5f), (int)(height* density + 0.5f));
                }
            });
        }
    }

    @Override
    protected void addEventEmitters(
            final ThemedReactContext reactContext,
            final CaptureView view) {
        view.setOnEvChangeListener(
                new CaptureView.OnEvChangeListener() {
                    @Override
                    public void getQRCodeResult(String result,BarcodeFormat format) {
                        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher()
                                .dispatchEvent(new QRCodeResultEvent(view.getId(), SystemClock.nanoTime(), result,format));
                    }

                });
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("QRCodeResult", MapBuilder.of("registrationName", "onBarCodeRead"))//registrationName 后的名字,RN中方法也要是这个名字否则不执行
                .build();
    }

/*

        @ReactProp(name = "aspect")
        public void setAspect(CaptureView view, int aspect) {
            view.setAspect(aspect);
        }

        @ReactProp(name = "captureMode")
        public void setCaptureMode(RCTCameraView view, int captureMode) {
            // TODO - implement video mode
        }

        @ReactProp(name = "captureTarget")
        public void setCaptureTarget(RCTCameraView view, int captureTarget) {
            // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
        }

        @ReactProp(name = "type")
        public void setType(RCTCameraView view, int type) {
            view.setCameraType(type);
        }
*/


}