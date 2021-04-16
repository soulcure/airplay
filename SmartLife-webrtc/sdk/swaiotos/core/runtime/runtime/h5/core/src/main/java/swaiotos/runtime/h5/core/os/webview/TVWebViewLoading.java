package swaiotos.runtime.h5.core.os.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import swaiotos.runtime.h5.R;

public class TVWebViewLoading {

    public interface TVWebViewLoadFailListener{
        void onWebViewLoadFail();
    }

    private TVWebViewLoadFailListener mListener;
    private LottieAnimationView mProgressBar;
    private Context mContext;
    private TextView mbottomTextView;
    private LinearLayout mLoadingLayout;
    private LoadingTextHandler mLoadingHandler;
    private LinearLayout mContentLayout;
    private ImageView mContentImgView;
    private TextView mContentTxtView;

    public void showLoadingView(){
        if(mLoadingLayout!=null){
            mLoadingLayout.setVisibility(View.VISIBLE);
        }
        if(mProgressBar!=null){
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setImageAssetsFolder("images");
            mProgressBar.setAnimation("images/loading.json");
            mProgressBar.playAnimation();
        }
        if(mbottomTextView!=null){
            mbottomTextView.setVisibility(View.VISIBLE);
            mbottomTextView.setText(R.string.loading_web);
        }

        if(mContentLayout!=null){
            mContentLayout.setVisibility(View.VISIBLE);
        }
        if(mContentImgView!=null){
            mContentImgView.setVisibility(View.VISIBLE);
        }
        if(mContentTxtView != null){
            mContentTxtView.setVisibility(View.VISIBLE);
        }
    }

    public void dismissLoadingView(){
        if(mProgressBar!=null){
            mProgressBar.cancelAnimation();
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        if(mbottomTextView!=null){
            mbottomTextView.setVisibility(View.INVISIBLE);
            mbottomTextView.setText("");
        }
        if(mLoadingLayout!=null){
            mLoadingLayout.setVisibility(View.INVISIBLE);
        }


        if(mContentImgView!=null){
            mContentImgView.setVisibility(View.INVISIBLE);
        }
        if(mContentTxtView != null){
            mContentTxtView.setText(R.string.miniprog_title);
            mContentTxtView.setVisibility(View.INVISIBLE);
        }

        if(mContentLayout!=null){
            mContentLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void showTitle(String title){
        if(mContentTxtView != null){
            mContentTxtView.setText(title);
        }
    }

    public  void showErrView(){
        if(mLoadingLayout!=null){
            mLoadingLayout.setVisibility(View.VISIBLE);
        }
        if(mProgressBar!=null){
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setImageAssetsFolder("images");
            mProgressBar.setAnimation("images/loadfailure.json");
            mProgressBar.playAnimation();
        }
        if(mbottomTextView!=null){
            mbottomTextView.setVisibility(View.VISIBLE);
            mbottomTextView.setText(R.string.loading_web);
        }
        if(mLoadingHandler!=null){
            Message msg = new Message();
            msg.what = 0;
            msg.arg1 = 15;
            mLoadingHandler.sendMessageDelayed(msg,1000);
        }
    }

    public TVWebViewLoading(Context context, TVWebViewLoading.TVWebViewLoadFailListener listener, LinearLayout loadingLayout,LinearLayout contentLayout){
        this.mContext = context;
        this.mLoadingLayout = loadingLayout;
        this.mContentLayout = contentLayout;
        this.mListener = listener;
        mLoadingHandler = new LoadingTextHandler(Looper.getMainLooper());
        this.mProgressBar = mLoadingLayout.findViewById(R.id.tv_lottie_loading);
        this.mbottomTextView = mLoadingLayout.findViewById(R.id.loadingText);
        this.mContentImgView = mContentLayout.findViewById(R.id.img_miniprog);
        this.mContentTxtView = mContentLayout.findViewById(R.id.txt_miniprog_title);
    }

    private class LoadingTextHandler extends Handler {

        public LoadingTextHandler() {
            super();
        }

        public LoadingTextHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                int curMsg = msg.arg1;
                if(mbottomTextView!=null){
                    String showText = mContext.getResources().getString(R.string.loading_fail);
                    showText = String.format(showText,curMsg);
                    mbottomTextView.setText(showText);
                }

                if(curMsg>0){
                    curMsg -= 1;
                    Message delayMsg = new Message();
                    delayMsg.what = 0;
                    delayMsg.arg1 = curMsg;
                    mLoadingHandler.sendMessageDelayed(delayMsg,1000);
                }else{
                    if(mLoadingHandler.hasMessages(0)){
                        mLoadingHandler.removeMessages(0);
                    }
                    if(mListener!=null){
                        mListener.onWebViewLoadFail();
                    }
                }

            }
        }
    }
}
