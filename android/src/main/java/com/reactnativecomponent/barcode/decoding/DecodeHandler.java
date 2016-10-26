/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactnativecomponent.barcode.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.reactnativecomponent.barcode.CaptureView;
import com.reactnativecomponent.barcode.R;
import com.reactnativecomponent.barcode.camera.CameraManager;
import com.reactnativecomponent.barcode.camera.PlanarYUVLuminanceSource;


import java.util.Hashtable;

final class
DecodeHandler extends Handler {

//  private static final String TAG = DecodeHandler.class.getSimpleName();
  private static final String TAG ="Test";


  private final CaptureView captureView;
  private final MultiFormatReader multiFormatReader;

  DecodeHandler(CaptureView captureView, Hashtable<DecodeHintType, Object> hints) {

    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.captureView = captureView;
  }

  @Override
  public void handleMessage(Message message) {
    int id=message.what;
      if(id== R.id.decode) {
        if(captureView.decodeFlag) {
          decode((byte[]) message.obj, message.arg1, message.arg2);
        }
      }else if(id==R.id.quit) {
//        Log.i(TAG, "decode quit");
          Looper.myLooper().quit();
      }

  }



  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    // Log.v("DecodeHandler", data.length + "");
//	  {
//		StringBuilder tmp;
//		// if the data width is 480
//		{
//			int x = 40;
//			tmp = new StringBuilder();
//			for (int y = 0; y < height; y += 20)
//				tmp.append(Integer.toHexString(data[y * width + x]) + "_");
//			Log.v("DecodeHandler", tmp.toString());
//		}
//		// if the data width is 320
//		{
//			int x = 40;
//			tmp = new StringBuilder();
//			for (int y = 0; y < width; y += 20)
//				tmp.append(Integer.toHexString(data[y * height + x]) + "_");
//			Log.v("DecodeHandler", tmp.toString());
//		}
//	  }
	// rotate the data 90 degree clockwise.
	// note that on a HTC G2, data length is 230400, but 480*320=153600,
	// which means u and v channels are not touched.
	// from the rotated data you will get a wrong image, whatever, only Y
	// channel is used.
	// check PlanarYUVLuminanceSource for more.
	// it says: "It works for any pixel format where
	// the Y channel is planar and appears first, including
	// YCbCr_420_SP and YCbCr_422_SP."
	byte[] rotatedData = new byte[data.length];
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++)
			rotatedData[x * height + height - y - 1] = data[x + y * width];
	}
	  
    long start = System.currentTimeMillis();
    Result rawResult = null;
//     PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);
//     PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, height, width);
    // switch width and height
//    Log.i("Test","DecodeHandler_height:"+height+",width:"+width);
    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, height, width);
    
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    try {
      rawResult = multiFormatReader.decodeWithState(bitmap);
    } catch (ReaderException re) {
      // continue
    } finally {
      multiFormatReader.reset();
    }

    if (rawResult != null) {
      long end = System.currentTimeMillis();
//      Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
      Message message = Message.obtain(captureView.getHandler(), R.id.decode_succeeded, rawResult);
      Bundle bundle = new Bundle();
      bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
      message.setData(bundle);
      //Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(captureView.getHandler(), R.id.decode_failed);
      message.sendToTarget();
    }
  }

}
