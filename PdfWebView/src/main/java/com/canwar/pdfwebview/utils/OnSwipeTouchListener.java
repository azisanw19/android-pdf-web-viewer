package com.canwar.pdfwebview.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;

    public OnSwipeTouchListener(Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(ctx, new ScaleGestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return false;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 300;
        private static final int SWIPE_VELOCITY_THRESHOLD = 300;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        return true;
                    }
                }
                /*else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    return true;
                }*/
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private final class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        final float SPAN_RATIO = 600;
        private static final int SCALE_THRESHOLD_ZOOM_OUT = 1000;
        private static final int SCALE_THRESHOLD_ZOOM_IN = 1000;

        float initialSpan;
        float prevNbStep;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initialSpan = detector.getCurrentSpan();
            prevNbStep = 0;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float spanDiff = initialSpan - detector.getCurrentSpan();
            float curNbStep = spanDiff / SPAN_RATIO;

            float stepDiff = curNbStep - prevNbStep;
            if (stepDiff > 0 && stepDiff < SCALE_THRESHOLD_ZOOM_OUT) {
                onZoomOut(stepDiff);
                return true;
            } else if (stepDiff < 0 && stepDiff < SCALE_THRESHOLD_ZOOM_IN) {
                onZoomIn(stepDiff);
                return true;
            }
            prevNbStep = curNbStep;

            return super.onScale(detector);
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(scaleGestureDetector);
        }
    }


    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onZoomIn(float stepDiff) {
    }

    public void onZoomOut(float stepDiff) {
    }
}