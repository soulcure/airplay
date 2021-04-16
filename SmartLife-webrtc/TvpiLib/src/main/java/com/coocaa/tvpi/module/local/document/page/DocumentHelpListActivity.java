package com.coocaa.tvpi.module.local.document.page;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import java.util.HashMap;
import java.util.Set;

import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 3/30/21
 */
public class DocumentHelpListActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_help_list);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.help_btn1).setOnClickListener(this);
        findViewById(R.id.help_btn2).setOnClickListener(this);
        findViewById(R.id.help_btn3).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.help_btn1) {
            startActivity(new Intent(this, DocumentAddHelpActivity.class));
        } else if (id == R.id.help_btn2) {
            click("https://webapp.skysrt.com/swaiot/novice-guide/index.html#/text?position=pageturn");
        } else if (id == R.id.help_btn3) {
            click("https://webapp.skysrt.com/swaiot/novice-guide/index.html#/text?position=remove");
        }
    }

    private void click(String url) {
        FunctionBean functionBean = new FunctionBean();
        functionBean.icon = "";
        Uri uri = Uri.parse(url);
        functionBean.type = uri.getScheme();
        functionBean.id = uri.getAuthority();
        functionBean.target = uri.getPath();
        String fragment = uri.getFragment();
        if (!TextUtils.isEmpty(fragment)) {
            functionBean.fragment = fragment;
        }
        Set<String> paramsKeySet = uri.getQueryParameterNames();
        if (paramsKeySet != null && !paramsKeySet.isEmpty()) {
            functionBean.params = new HashMap<>();
            for (String key : paramsKeySet) {
                functionBean.params.put(key, uri.getQueryParameter(key));
            }
        }
        functionBean.name = "文档帮助页面";
        functionBean.runtime = new HashMap<>();
        functionBean.runtime.put(H5RunType.RUNTIME_NETWORK_FORCE_KEY, H5RunType.RUNTIME_NETWORK_FORCE_LAN);
        functionBean.runtime.put(H5RunType.RUNTIME_NAV_KEY, H5RunType.RUNTIME_NAV_FLOAT);
        TvpiClickUtil.onClick(this, functionBean.uri());
    }

}
