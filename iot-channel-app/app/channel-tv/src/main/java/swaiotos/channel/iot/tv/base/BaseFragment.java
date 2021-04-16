package swaiotos.channel.iot.tv.base;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;

public class BaseFragment extends android.support.v4.app.Fragment {

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";

    public String mParam1;
    public String mParam2;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
