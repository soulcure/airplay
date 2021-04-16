package com.coocaa.tvpi.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SoftKeyBoardUtil {
    public static void hideKeyBoard(Context context ,EditText editText) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void openKeyBoard(Context context,EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
        editText.setSelection(editText.getText().length());
    }
}
