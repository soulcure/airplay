package com.coocaa.tvpi.module.mall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import java.util.Objects;

/**
 * 发票信息
 * Created by songxing on 2020/8/26
 */
public class InvoiceInfoActivity extends BaseActivity {
    private static final String TAG = InvoiceInfoActivity.class.getSimpleName();
    public static final int INVOICE_TYPE_NON = 0;
    public static final int INVOICE_TYPE_PERSON = 1;
    public static final int INVOICE_TYPE_COMPANY = 2;

    private CommonTitleBar titleBar;
    private TextView tvInvoiceType;
    private RadioGroup radioGroup;
    private RadioButton rbPerson;
    private RadioButton rbCompany;
    private LinearLayout companyLayout;
    private AppCompatEditText etInvoiceTitle;
    private AppCompatEditText etInvoiceTax;

    // 0无发票 1个人 2单位
    private int invoiceType;
    private String invoiceTitle;
    private String invoiceTax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_info);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        parseIntent();
        initView();
        setListener();
    }

    private void parseIntent() {
        if (getIntent() != null) {
            invoiceType = getIntent().getIntExtra("type", INVOICE_TYPE_PERSON);
            invoiceTitle = getIntent().getStringExtra("invoiceTitle");
            invoiceTax = getIntent().getStringExtra("invoiceTax");
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        tvInvoiceType = findViewById(R.id.tvInvoiceType);
        radioGroup = findViewById(R.id.radioGroup);
        rbPerson = findViewById(R.id.rbPersonally);
        rbCompany = findViewById(R.id.rbCompany);
        companyLayout = findViewById(R.id.companyLayout);
        etInvoiceTitle = findViewById(R.id.etInvoiceTitle);
        etInvoiceTax = findViewById(R.id.etInvoiceNumber);
        if(invoiceType == INVOICE_TYPE_NON){
            invoiceType = INVOICE_TYPE_PERSON;
        }
        if (invoiceType == INVOICE_TYPE_COMPANY) {
            rbPerson.setChecked(false);
            rbCompany.setChecked(true);
            companyLayout.setVisibility(View.VISIBLE);
            etInvoiceTitle.setText(invoiceTitle);
            etInvoiceTax.setText(invoiceTax);
        } else if(invoiceType == INVOICE_TYPE_PERSON){
            rbPerson.setChecked(true);
            rbCompany.setChecked(false);
            companyLayout.setVisibility(View.GONE);
            etInvoiceTitle.setText(invoiceTitle);
            etInvoiceTax.setText(invoiceTax);
        }
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    if (!verify()) {
                        ToastUtils.getInstance().showGlobalShort("请先完善信息");
                        return;
                    }
                    Intent intent = new Intent(InvoiceInfoActivity.this, ConfirmOrderActivity.class);
                    intent.putExtra("invoiceType", invoiceType);
                    intent.putExtra("invoiceTitle", Objects.requireNonNull(etInvoiceTitle.getText()).toString());
                    intent.putExtra("invoiceTax", Objects.requireNonNull(etInvoiceTax.getText()).toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPersonally) {
                    invoiceType = INVOICE_TYPE_PERSON;
                    companyLayout.setVisibility(View.GONE);
                    tvInvoiceType.setText("普通发票-个人");
                } else {
                    invoiceType = INVOICE_TYPE_COMPANY;
                    companyLayout.setVisibility(View.VISIBLE);
                    tvInvoiceType.setText("普通发票-公司");
                }
            }
        });
    }

    private boolean verify() {
        if (invoiceType == INVOICE_TYPE_PERSON) {
            return true;
        } else {
            return etInvoiceTitle.getText() != null
                    && !TextUtils.isEmpty(etInvoiceTitle.getText().toString())
                    && etInvoiceTax.getText() != null
                    && !TextUtils.isEmpty(etInvoiceTax.getText().toString());
        }
    }
}
