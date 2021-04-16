package com.coocaa.tvpi.util;


import com.coocaa.smartscreen.data.app.TvAppModel;

import java.util.Comparator;

public class PinyinComparator implements Comparator<TvAppModel> {

    @Override
    public int compare(TvAppModel o1, TvAppModel o2) {
        if (o1.sortLetters.equals("@")
                || o2.sortLetters.equals("#")) {
            return -1;
        } else if (o1.sortLetters.equals("#")
                || o2.sortLetters.equals("@")) {
            return 1;
        } else {
            return o1.sortLetters.compareTo(o2.sortLetters);
        }
    }
}
