//package com.coocaa.tvpi.module.remote;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.widget.RelativeLayout;
//
//import com.coocaa.tvpilib.R;
//
///**
// * @ClassName RemoteTouchView
// * @User heni
// * @Date 2020/4/17
// */
//public class RemoteTouchView extends RelativeLayout {
//    private final static String TAG = RemoteTouchView.class.getSimpleName();
//    protected static final int OPTION_LEFT = 1;
//    protected static final int OPTION_RIGHT = 2;
//    protected static final int OPTION_UP = 3;
//    protected static final int OPTION_DOWN = 4;
//    protected static final int OPTION_CONFIRM = 5;
//    protected int mOptionMode = OPTION_CONFIRM;
//
//    private Context mContext;
//    private RemoteTouchCallback mRemoteTouchCallback;
//
//    //触摸的X
//    protected float mDownX;
//    //触摸的Y
//    protected float mDownY;
//    //手势偏差值
//    protected int mThreshold = 150;
//
//    public interface RemoteTouchCallback {
//        void onActionDown();
//
//        void onActionUp();
//
//        void onConfirm();
//
//        void onMoveLeft();
//
//        void onMoveRight();
//
//        void onMoveTop();
//
//        void onMoveBottom();
//    }
//
//    public void setRemoteCtrlCallback(RemoteTouchCallback remoteCtrlCallback) {
//        mRemoteTouchCallback = remoteCtrlCallback;
//    }
//
//    public RemoteTouchView(Context context) {
//        super(context);
//        mContext = context;
//        initView();
//    }
//
//    public RemoteTouchView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mContext = context;
//        initView();
//    }
//
//    public RemoteTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        mContext = context;
//        initView();
//    }
//
//    private void initView() {
//        LayoutInflater inflater =
//                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(R.layout.remote_touch_view, this);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //从父控件获取控制权，左右滑动的时候 viewpager不会左右滑动
//        getParent().requestDisallowInterceptTouchEvent(true);
//        float x = event.getX();
//        float y = event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
////                Log.d(TAG, "ACTION_DOWN: ");
//                mDownX = x;
//                mDownY = y;
//                if (null != mRemoteTouchCallback) {
//                    mRemoteTouchCallback.onActionDown();
//                }
//                return true;
//            case MotionEvent.ACTION_MOVE:
////                Log.d(TAG, "ACTION_MOVE: ");
//                float deltaX = x - mDownX;
//                float deltaY = y - mDownY;
//                float absDeltaX = Math.abs(deltaX);
//                float absDeltaY = Math.abs(deltaY);
//
//                handleMove(absDeltaX, absDeltaY, deltaX, deltaY);
//                return true;
//            case MotionEvent.ACTION_UP:
////                Log.d(TAG, "ACTION_UP: ");
////                handleOption();
//                handleUp();
//                return true;
//        }
//        return super.onTouchEvent(event);
//    }
//
//    private void handleMove(float absDeltaX, float absDeltaY, float deltaX, float deltaY) {
//        if (mOptionMode != OPTION_CONFIRM) {
//            return;
//        }
//
//        if (absDeltaX > mThreshold && absDeltaY > mThreshold) {
//            if (absDeltaX > absDeltaY) {
//                if (deltaX < 0) {
////                    Log.d(TAG, "handleMove: move left");
//                    mOptionMode = OPTION_LEFT;
//                } else {
////                    Log.d(TAG, "handleMove: move right");
//                    mOptionMode = OPTION_RIGHT;
//                }
//            } else {
//                if (deltaY < 0) {
////                    Log.d(TAG, "handleMove: move top");
//                    mOptionMode = OPTION_UP;
//                } else {
////                    Log.d(TAG, "handleMove: move bottom");
//                    mOptionMode = OPTION_DOWN;
//                }
//            }
//        } else if (absDeltaX >= mThreshold) {
//            if (deltaX < 0) {
////                Log.d(TAG, "handleMove: move left");
//                mOptionMode = OPTION_LEFT;
//            } else {
////                Log.d(TAG, "handleMove: move right");
//                mOptionMode = OPTION_RIGHT;
//            }
//        } else if (absDeltaY >= mThreshold) {
//            if (deltaY < 0) {
////                Log.d(TAG, "handleMove: move top");
//                mOptionMode = OPTION_UP;
//            } else {
////                Log.d(TAG, "handleMove: move bottom");
//                mOptionMode = OPTION_DOWN;
//            }
//        }/* else {
//            Log.d(TAG, "handleMove: confirm");
//            mOptionMode = OPTION_CONFIRM;
//        }*/
//
//        switch (mOptionMode) {
//            case OPTION_UP:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveTop();
//                break;
//            case OPTION_DOWN:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveBottom();
//                break;
//            case OPTION_LEFT:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveLeft();
//                break;
//            case OPTION_RIGHT:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveRight();
//                break;
//            default:
//                break;
//        }
//        Log.d(TAG, "handleMove: " + mOptionMode);
//    }
//
//    private void handleUp() {
//        if (mOptionMode == OPTION_CONFIRM) {
//            if (null != mRemoteTouchCallback)
//                mRemoteTouchCallback.onConfirm();
//        } else {
//            mOptionMode = OPTION_CONFIRM;
//            if (null != mRemoteTouchCallback)
//                mRemoteTouchCallback.onActionUp();
//        }
//    }
//
//    private void handleOption() {
//        switch (mOptionMode) {
//            case OPTION_UP:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveTop();
//                break;
//            case OPTION_DOWN:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveBottom();
//                break;
//            case OPTION_LEFT:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveLeft();
//                break;
//            case OPTION_RIGHT:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onMoveRight();
//                break;
//            case OPTION_CONFIRM:
//                if (null != mRemoteTouchCallback)
//                    mRemoteTouchCallback.onConfirm();
//                break;
//            default:
//                break;
//        }
//        mOptionMode = OPTION_CONFIRM;//操作完成要重置为OPTION_CONFIRM 要不单击事件会无效
//    }
//}
