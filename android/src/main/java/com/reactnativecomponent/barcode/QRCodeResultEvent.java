package com.reactnativecomponent.barcode;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.zxing.BarcodeFormat;


public class QRCodeResultEvent extends Event<QRCodeResultEvent> {
        String result;
    BarcodeFormat format;
    public QRCodeResultEvent(int viewTag, long timestampMs,String result,BarcodeFormat format) {
//        super(viewTag, timestampMs);
        super(viewTag);
        this.result=result;
        this.format=format;
    }

    @Override
    public String getEventName(){
        return "QRCodeResult";
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableMap eventData = Arguments.createMap();
        WritableMap data = Arguments.createMap();
        data.putString("code", getResult());
        data.putString("type",format.toString());
//        Log.i("Test","code="+getResult());
        eventData.putMap("data",data);


        return eventData;
    }

    public String getResult() {
        return result;
    }

    public BarcodeFormat getFormat() {
        return format;
    }
}
