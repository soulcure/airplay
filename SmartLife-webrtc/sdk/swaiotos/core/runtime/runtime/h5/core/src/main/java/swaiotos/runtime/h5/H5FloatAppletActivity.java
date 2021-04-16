package swaiotos.runtime.h5;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @Author: yuzhan
 */
public class H5FloatAppletActivity extends BaseH5AppletActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mHeaderHandler != null) {
            mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    @Override
    protected H5RunType.RunType runType() {
        return H5RunType.RunType.MOBILE_RUNTYPE_ENUM;
    }

//    class H5MobileFloatAppletLayoutBuilder implements LayoutBuilder {
//        private Context mContext;
//
//        public H5MobileFloatAppletLayoutBuilder(Context context) {
//            this.mContext = context;
//        }
//
//        @Override
//        public View build(View content) {
//            LayoutInflater inflater = LayoutInflater.from(mContext);
//            FrameLayout root = (FrameLayout) inflater.inflate(R.layout.h5_float_nav_layout, null);
//            View nav = root.findViewById(R.id.nav);
//            nav.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LogUtil.d("close onClick() called with: v = [" + v + "]");
//                    finish();
//                }
//            });
//            nav.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LogUtil.d("more onClick() called with: v = [" + v + "]");
//                    share();
//                }
//            });
//            root.addView(content, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            return root;
//        }
//    }

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        return new H5MobileFloatAppletLayoutBuilder(this);
//        return new H5MobileFloatAppletLayoutBuilder(this);
    }

    @Override
    public int getSafeDistanceTop() {
        return (int) getResources().getDimension(R.dimen.h5_top_height);
    }
}
