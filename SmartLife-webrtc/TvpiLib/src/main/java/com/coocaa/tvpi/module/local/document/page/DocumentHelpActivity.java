package com.coocaa.tvpi.module.local.document.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.tvpilib.R;


/**
 * @Description: 文档帮助页面
 * @Author: wzh
 * @CreateDate: 2020/10/23
 */
public class DocumentHelpActivity extends BaseAppletActivity {
    public static final int TYPE_HELP1 = 1;
    public static final int TYPE_HELP2 = 2;
    private static final String TYPE_NAME = "type";
    private DocumentHelp1View help1_view;
    private DocumentHelp2View help2_view;

    public static void start(Context context, int type) {
        Intent intent = new Intent(context, DocumentHelpActivity.class);
        intent.putExtra(TYPE_NAME, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_help_layout);
        initViews();
        if (mHeaderHandler != null) {
            if (mNPAppletInfo != null) {
                mHeaderHandler.setTitle("文档帮助");
            }
        }
    }

    protected void initViews() {
        int type = getIntent().getIntExtra(TYPE_NAME, TYPE_HELP1);
        help1_view = findViewById(R.id.help1_view);
        help2_view = findViewById(R.id.help2_view);
        if (TYPE_HELP1 == type) {
            help1_view.setVisibility(View.VISIBLE);
        } else {
            help2_view.setVisibility(View.VISIBLE);
        }
    }
}
