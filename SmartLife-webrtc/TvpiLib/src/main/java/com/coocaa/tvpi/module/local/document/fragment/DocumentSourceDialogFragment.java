package com.coocaa.tvpi.module.local.document.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.app.bean.DocumentSelectItemBean;
import com.coocaa.tvpi.module.local.document.DocLogSubmit;
import com.coocaa.tvpi.module.local.document.page.DocumentListActivity;
import com.coocaa.tvpi.module.local.document.page.DocumentMainActivity;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.FileChooseUtils;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;


/**
 * @ClassName ConnectDialogFragment
 * @Description 文档选择来源弹框
 * @User luoxi
 * @Date 2020-12-2
 * @Version TODO (write something)
 */
public class DocumentSourceDialogFragment extends BottomBaseDialogFragment {
    private static final String TAG = DocumentSourceDialogFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private BaseQuickAdapter<DocumentSelectItemBean, BaseViewHolder> mAdapter;
    private final static String SOURCE_FILES = "Files";
    private final static int REQUEST_CODE = 1;

    public DocumentSourceDialogFragment(AppCompatActivity mActivity) {
        super(mActivity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.source_document_dialog_layout, container, false);
    }

    protected void initViews(View view) {
        View.OnClickListener closeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        };
        view.findViewById(R.id.main_layout).setOnClickListener(closeListener);
        view.findViewById(R.id.bt_close).setOnClickListener(closeListener);
        StringBuilder builder = new StringBuilder();
        builder.append("*当前版本支持");
        for (FormatEnum format : DocumentConfig.SUPPORT_FORMATS) {
            builder.append(format.type).append("、");
        }
        builder.deleteCharAt(builder.lastIndexOf("、"));
        builder.append("格式*");
        ((TextView) view.findViewById(R.id.title3)).setText(builder.toString());
        recyclerView = view.findViewById(R.id.source_list_recyclerview);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(mActivity, 2);
        recyclerView.setLayoutManager(linearLayoutManager);
        // CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(DimensUtils.dp2Px(mActivity, 10f), DimensUtils.dp2Px(mActivity, 10f), 0);
        //  recyclerView.addItemDecoration(decoration);
        List<DocumentSelectItemBean> datas = new ArrayList<>();
        String[] names = getResources().getStringArray(R.array.document_source_name_list);
        String[] source = {DocumentConfig.Source.ALL.text, SOURCE_FILES, DocumentConfig.Source.WEIXIN.text, DocumentConfig.Source.WEIXINWORK.text,
                DocumentConfig.Source.DINGTALK.text, DocumentConfig.Source.QQ.text};
        int[] icons = {R.drawable.icon_source_scan, R.drawable.icon_source_files, R.drawable.icon_source_wechat,
                R.drawable.icon_source_enterprise_wechat, R.drawable.icon_source_dingding, R.drawable.icon_source_qq};
        for (int i = 0; i < names.length; i++) {
            datas.add(new DocumentSelectItemBean(names[i], source[i], icons[i]));
        }
        view.findViewById(R.id.item_parent).setPadding(0, 0, 0, getNavigationBarHeight(mActivity));
        mAdapter = new BaseQuickAdapter<DocumentSelectItemBean, BaseViewHolder>(R.layout.item_document_source_select, datas) {
            @Override
            protected void convert(@NotNull BaseViewHolder holder, DocumentSelectItemBean bean) {
                holder.setText(R.id.name, bean.getSourceName());
                holder.setImageResource(R.id.icon, bean.getResId());
            }
        };
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String source = mAdapter.getItem(position).getSource();
                if (source.equals(SOURCE_FILES)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/*");
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    DocumentConfig.Source src = DocumentConfig.getSourceByText(source);
                    if (DocumentUtil.isAndroidR() && (src == DocumentConfig.Source.QQ || src == DocumentConfig.Source.WEIXIN)) {
                        new DocumentHelpVideoDialogFragment(mActivity, src).show();
                    } else {
                        Intent intent = new Intent(getContext(), DocumentListActivity.class);
                        intent.putExtra(DocumentUtil.KEY_SCAN_SOURCE, source);
                        mActivity.startActivity(intent);
//                        if (mActivity instanceof NPAppletActivity) {
//                            ((NPAppletActivity) mActivity).startActivity(intent);
//                        }
                    }
                    dismissDialog();
                }
                Device device = SSConnectManager.getInstance().getDevice();
                CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
                LogParams params = LogParams.newParams().append("add_type", getAddType(source))
                        .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                        .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                        .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id());
                DocLogSubmit.submit(DocLogSubmit.EVENTID_CLICK_ADD_DOC_TYPE, params.getParams());
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    private String getAddType(String source) {
        String addType = "";
        if (SOURCE_FILES.equals(source)) {
            addType = "file_browser";
        } else if (DocumentConfig.Source.ALL.text.equals(source)) {
            addType = "scan_file";
        } else if (DocumentConfig.Source.WEIXIN.text.equals(source)) {
            addType = "wechat";
        } else if (DocumentConfig.Source.WEIXINWORK.text.equals(source)) {
            addType = "wecom";
        } else if (DocumentConfig.Source.DINGTALK.text.equals(source)) {
            addType = "DingTalk";
        } else if (DocumentConfig.Source.QQ.text.equals(source)) {
            addType = "qq";
        }
        return addType;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && REQUEST_CODE == requestCode) {
            if (data != null && data.getData() != null) {
                Log.i(TAG, "onActivityResult uri: " + data.getDataString());
                String path = null;
                try {
                    path = FileChooseUtils.getPath(mActivity, data.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onActivityResult path: " + path);
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file.exists()) {
                        String suffix = DocumentUtil.getFileType(path);
                        if (DocumentConfig.SUPPORT_FORMATS.contains(FormatEnum.getFormat(suffix))) {
                            Intent intent = new Intent(mActivity, DocumentMainActivity.class);
                            intent.putExtra(DocumentUtil.KEY_FILE_PATH, path);
                            intent.putExtra(DocumentUtil.KEY_FILE_SIZE, String.valueOf(file.length()));
                            intent.putExtra(DocumentUtil.KEY_SOURCE_APP, "手机文件夹");
                            intent.putExtra(DocumentUtil.KEY_SOURCE_PAGE, DocumentUtil.SOURCE_PAGE_OTHER_APP);
                            mActivity.startActivity(intent);
//                            if (mActivity instanceof NPAppletActivity) {
//                                ((NPAppletActivity) mActivity).startActivity(intent);
//                            }
                            checkFilePath(file);
                        } else {
                            ToastUtils.getInstance().showGlobalLong("不支持打开此类型");
                        }
                    } else {
                        ToastUtils.getInstance().showGlobalLong("文件不存在");
                    }
                } else {
                    ToastUtils.getInstance().showGlobalLong("错误文件");
                }
            }
        }
        dismissDialog();
    }

    private void checkFilePath(File file) {
        try {
            String fileDirPath = file.getParentFile().getAbsolutePath();
            Log.i(TAG, "checkFilePath: fileDirPath--" + fileDirPath);
            if (DocumentUtil.PATH_EXTERNAL.equals(fileDirPath)) {
                return;
            }
            for (String path : DocumentConfig.ALL_SCAN_PATHS) {
                if (fileDirPath.equals(DocumentUtil.PATH_EXTERNAL + "/" + path)) {
                    return;
                }
            }
            List<String> paths = SpUtil.getList(mActivity, DocumentUtil.SP_KEY_SCAN_PATH);
            if (paths == null) {
                paths = new ArrayList<>();
            }
            if (!paths.contains(fileDirPath)) {
                paths.add(fileDirPath);
                Log.i(TAG, "checkFilePath--> save: " + fileDirPath);
                SpUtil.putList(mActivity, DocumentUtil.SP_KEY_SCAN_PATH, paths);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
