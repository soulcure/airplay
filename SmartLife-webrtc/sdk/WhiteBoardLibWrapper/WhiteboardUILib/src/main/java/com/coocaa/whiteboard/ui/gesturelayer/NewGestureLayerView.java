/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.coocaa.whiteboard.ui.gesturelayer;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.coocaa.whiteboard.ui.gesturelayer.detector.CPathGestureDetector;
import com.coocaa.whiteboard.ui.util.WhiteboardUIConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class NewGestureLayerView extends GestureLayerView {
    CPathGestureDetector pathGestureDetector;
    public NewGestureLayerView(@NonNull Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null); //华为pad android 10 导致卡顿
        pathGestureDetector = new CPathGestureDetector(new PathListener());
        setGestureStrokeWidth(WhiteboardUIConfig.DEFAULT_PAINT_SIZE);
//        pathGestureDetector.setRewind(false);
//        paint.setColor(Color.CYAN);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(1920*3, 1080*3);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return false;
    }
    public void handleTouchEvent(MotionEvent event) {
        pathGestureDetector.onTouchEvent(event);
    }

    private boolean touchUp = false ;
    private class PathListener extends CPathGestureDetector.SimpleGestureDetectorListener {
        @Override
        public void onPathChange(@Nullable Rect invalidRect, Path changePath) {
//            if (!touchUp)
//            {
//
//            }
            Log.e("k3", "onPathChange");
            path = changePath;
            invalidate();  // 修复华为10 pad 画线抖动
        }

        @Override
        public void onGestureStarted(MotionEvent event) {
            touchUp = false;
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureStarted(event);
            }
        }

        @Override
        public void onGesture(MotionEvent event) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGesture(event);
            }
        }

        @Override
        public void onGestureEnded(MotionEvent event) {
            touchUp = true;
            Log.e("k3", "onGestureEnded");
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureEnded(event);
            }
        }

        @Override
        public void onGestureCancelled(MotionEvent event) {
            touchUp = false;
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureCancelled(event);
            }
        }
    }
}
