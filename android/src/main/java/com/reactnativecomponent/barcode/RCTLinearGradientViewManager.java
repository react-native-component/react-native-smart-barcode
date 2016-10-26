package com.reactnativecomponent.barcode;



import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativecomponent.barcode.view.LinearGradientView;

public class RCTLinearGradientViewManager extends SimpleViewManager<LinearGradientView>{

    private static final String REACT_CLASS = "LinearGradientView";//要与类名一致
    LinearGradientView linearGradientView;
    private float density;
    Activity activity;


    public RCTLinearGradientViewManager(Activity activity) {
        this.activity=activity;
        density = activity.getResources().getDisplayMetrics().density;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }
    @Override
    protected LinearGradientView createViewInstance(ThemedReactContext reactContext) {
        linearGradientView=new LinearGradientView(reactContext,activity);
        return linearGradientView;

    }


    @ReactProp(name = "size" ,defaultInt = 1)
    public void setSize(LinearGradientView view,int size) {
        int num=(int)(size*density+0.5f);
        if(num<3){
            num=3;
        }else if(num >10){
            num=5;
        }
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        params.height=num;
        params.width =view.width;
        view.setLayoutParams(params);
        view.size=num;

    }


    @ReactProp(name = "width" ,defaultInt = 0)
    public void setWidth(LinearGradientView view, int width) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
if((int) (width * density + 0.5f)>1) {
    params.width = (int) (width * density + 0.5f);
    params.height=view.size;
}
        view.width=(int)(width*density+0.5f);
        view.setLayoutParams(params);


    }



    @ReactProp(name = "frameColor")
    public void setFrameColor(LinearGradientView view, String color) {
        if (color != null && !color.isEmpty()) {
            view.setFrameColor(Color.parseColor(color));//转换成16进制
        }
    }


    @Override
    public void setBackgroundColor(LinearGradientView view, int backgroundColor) {
       // super.setBackgroundColor(view, backgroundColor);
    }
}
