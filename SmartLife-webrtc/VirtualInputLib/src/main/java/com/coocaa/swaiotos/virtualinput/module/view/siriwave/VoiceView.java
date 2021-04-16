package com.coocaa.swaiotos.virtualinput.module.view.siriwave;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.swaiotos.virtualinput.utils.VoiceTipsUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoiceView extends RelativeLayout {

    private static final String TAG = VoiceView.class.getSimpleName();

    private Context mContext;
    private boolean isWaveStop;

    private int[] mTipsTVId = {R.id.tips_tv1, R.id.tips_tv2, R.id.tips_tv3, R.id.tips_tv4, R.id.tips_tv5, R.id.tips_tv6};
    private List<TextView> mTipTVList;
    private TextView tvContent;
    private View mTipsLayout;
    private SiriWaveView siriWaveView;
    File soundFile = null;

    private VoiceCallback mVoiceCallback;

    private final MyHandler mHandler = new MyHandler(this);
    private boolean isHind = false;

    public void setVolume(final int volume) {
        if (siriWaveView != null) {
            siriWaveView.setVolume(volume);
        }
    }

    private static class MyHandler extends Handler {

        //对Activity的弱引用
        private final WeakReference<VoiceView> mActivity;

        public MyHandler(VoiceView activity) {
            mActivity = new WeakReference<VoiceView>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

        }
    }

    public VoiceView(Context context) {
        this(context, null);
    }

    public VoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public interface VoiceCallback {
        void onHide();
    }

    public void setVoiceCallback(VoiceCallback voiceCallback) {
        this.mVoiceCallback = voiceCallback;
    }

    public void setWaveStop(boolean waveStop) {
        isWaveStop = waveStop;
    }

    public void setVoiceContent(String content) {
        if (!isHind) {
            tvContent.setVisibility(VISIBLE);
            tvContent.setText(content);
            //setMaxEcplise(tvContent, 2, content);
        }
    }

    public void show() {
        isHind = false;
        String[] tips = VoiceTipsUtils.getTips();
        List<String> randomTips = randomTips(tips);
        if (tips != null && tips.length >= 0) {
            int index = 0;
            for (TextView textView : mTipTVList) {
                try {
                    if(index == 0) {
                        textView.setText("你可以试着说");
                    } else if(index == mTipTVList.size() - 1){
                        textView.setText("......");
                    }else {
                        textView.setText(randomTips.get(index));
                    }
                    showTipsAnim(textView, 100 * index);
                }catch (Exception e){
                    e.printStackTrace();
                }
                index ++;
            }
        }
        setVisibility(VISIBLE);
    }

    private List<String> randomTips(String[] tips) {
        List<String> tempTips = new ArrayList<>();
        Random random = new Random();
        int index = random.nextInt(tips.length);
        while (tempTips.size() < tips.length) {
            tempTips.add(tips[index]);
            index++;
            if (index == tips.length) {
                index = 0;
            }
        }
        return tempTips;
    }

    public void hide() {
        isHind = true;
        tvContent.setText("");
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", 0f, DimensUtils
                .dp2Px(mContext, 100));
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mTipsLayout, pvhX,
                pvhY).setDuration(100);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //还原ui
                mTipsLayout.setAlpha(1f);
                ObjectAnimator translationYAnim = ObjectAnimator.ofFloat(mTipsLayout,
                        "translationY", DimensUtils.dp2Px(mContext, 100), 0f);
                translationYAnim.setDuration(1).start();
                for (TextView tv :
                        mTipTVList) {
                    tv.setVisibility(GONE);
                }
                if (tvContent.getVisibility() != GONE) {
                    tvContent.setVisibility(GONE);
                }
                setVisibility(GONE);
                if (null != mVoiceCallback)
                    mVoiceCallback.onHide();
            }
        });
        objectAnimator.start();
    }

    public void hindTips() {
        for (TextView tv :
                mTipTVList) {
            tv.setVisibility(GONE);
        }
    }

    public void showWaveInAnim() {
        siriWaveView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showWaveInAnim: ");
                siriWaveView.startAnim();
            }
        });
    }

    public void showWaveOutAnim() {
        siriWaveView.stopAnim();
        if (soundFile != null) {
            soundFile.delete();
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.remote_voice_view, this);

        mTipsLayout = findViewById(R.id.tips_layout);
        mTipTVList = new ArrayList<>();
        for (int id :
                mTipsTVId) {
            TextView tip = findViewById(id);
            mTipTVList.add(tip);
        }

        siriWaveView = findViewById(R.id.siriWaveView);
        tvContent = findViewById(R.id.tv_content);
    }

    private void showTipsAnim(final View view, long delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", DimensUtils.dp2Px(mContext, 100), 0f);
                ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).setDuration(200).start();
            }
        }, delay);
    }

    private void createSoundFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), "sound");
        //指定录音输出文件的文件夹（最后会删除录音文件的）
        if (!dir.exists()) {
            //文件夹路径不存在就创建一个
            dir.mkdirs();
        }
        soundFile = new File(dir, "siri_record" + ".amr");
        //创建输出文件
        if (!soundFile.exists()) {
            try {
                soundFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 参数：maxLines 要限制的最大行数
     * 参数：content  指TextView中要显示的内容
     */
    public void setMaxEcplise(final TextView mTextView, final int maxLines, final String content) {
        Log.d(TAG, "setMaxEcplise: " + mTextView.getLineCount());

        if (mTextView.getLineCount() > maxLines) {
            int lineEndIndex = mTextView.getLayout().getLineEnd(maxLines - 1);
            //下面这句代码中：我在项目中用数字3发现效果不好，改成1了
            if (content.length() > lineEndIndex) {
                String text = "..." + content.subSequence(content.length() - lineEndIndex, content.length());
                mTextView.setText(text);
            }
        } else {
            mTextView.setText(content);
        }

    }
}
