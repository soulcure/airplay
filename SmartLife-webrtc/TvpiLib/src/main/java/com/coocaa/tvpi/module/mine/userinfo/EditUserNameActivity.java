package com.coocaa.tvpi.module.mine.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.views.SDialog;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpilib.R;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * @ClassName EditUserNameActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/8
 */
public class EditUserNameActivity extends BaseViewModelActivity<UserInfoViewModel> implements UnVirtualInputable {
    private String TAG = EditUserNameActivity.class.getSimpleName();
    private TextView cancelBtn, saveBtn;
    private EditText etName;
    private ImageView deleteBtn;

    private String mDefaultName;
    private int mErrorCode;
    SDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_editname);

        mDefaultName = getIntent().getStringExtra("USER_NAME");
        initView();
        initListener();

        //打开软键盘
        etName.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void initView() {
        cancelBtn = findViewById(R.id.cancel_btn);
        saveBtn = findViewById(R.id.save_btn);
        etName = findViewById(R.id.user_et_name);
        deleteBtn = findViewById(R.id.delete_btn);
        if (!TextUtils.isEmpty(mDefaultName)) {
            etName.setText(mDefaultName);
        }
        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
    }

    private void initListener() {
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newNickName = etName.getText().toString();
                if(!TextUtils.isEmpty(mDefaultName) && mDefaultName.equals(newNickName)) {
                    finish();
                    return;
                }
                mErrorCode = viewModel.isNameCorrect(newNickName);
                if (mErrorCode == 0) {
                    updateUserInfo(newNickName);
                } else {
                    String content = "";
                    if (mErrorCode == UserInfoViewModel.ERROR1) {
                        content = "没有输入昵称，请重新填写";
                    } else if (mErrorCode == UserInfoViewModel.ERROR2) {
                        content = "昵称只支持中英文和数字，请修改昵称并重试";
                    }
                    showErrorDialog(content);
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etName.setText("");
            }
        });

        etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateUserInfo(etName.getText().toString());
                }
                return false;
            }
        });
    }

    //修改nickName
    private void updateUserInfo(String nickName) {
        viewModel.updateUserInfo(UserInfoCenter.getInstance()
                .getAccessToken(), nickName, null, null)
                .observe(this, updateUserInfoObserver);
    }

    private Observer<String> updateUserInfoObserver = new Observer<String>() {
        @Override
        public void onChanged(String response) {
            if (!TextUtils.isEmpty(response) && response.equals("true")) {
                Log.d(TAG, "nick name modify success. ");
                //修改成功
                Intent intent = new Intent();
                intent.putExtra("NIKE_NAME", etName.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }else {
                showErrorDialog(response);
            }
        }
    };

    private void showErrorDialog(String errorMsg) {
        dialog = new SDialog(EditUserNameActivity.this, errorMsg,  "我知道了",
                new SDialog.SDialogListener() {
                    @Override
                    public void onOK() {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }
}
