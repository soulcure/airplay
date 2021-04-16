package com.coocaa.tvpi.module.mine.lab

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.coocaa.publib.base.BaseActivity
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.smartscreen.constant.SmartConstans
import com.coocaa.smartscreen.utils.SuperSpUtil
import com.coocaa.tvpi.module.io.HomeIOThread
import com.coocaa.tvpi.module.mine.view.VerificationCodeDialog
import com.coocaa.tvpilib.R

/**
 * @Author: yuzhan
 */
class SmartConfigActivity: BaseActivity() {

    lateinit var layout: LinearLayout
    lateinit var curServer: TextView
    lateinit var switchButton: Button
    lateinit var dialog: ConfirmDialog
    var isTest: Boolean = false
    val TAG = "SmartConfig"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        refreshState()
    }

    fun refreshState() {
        isTest = isTestServer()
        if(isTest) {
            curServer.setText("当前是测试环境")
            switchButton.setText("切换到正式环境")
        } else {
            curServer.setText("当前是正式环境")
            switchButton.setText("切换到测试环境")
        }
    }

    fun initView() {
        layout = LinearLayout(this)
        layout.apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            setContentView(layout)
        }
        curServer = TextView(this)
        curServer.apply {
            setTextColor(Color.BLACK)
            textSize = 22F
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0, 20, 0, 20)
            paint.isFakeBoldText = true
        }
        layout.addView(curServer)

        switchButton = Button(this)
        switchButton.apply {
            setTextColor(Color.BLACK)
            textSize = 18F
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        layout.addView(switchButton)
        switchButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                if(SmartConstans.isTestBuildChannel()) {
                    ToastUtils.getInstance().showGlobalShort("测试渠道APK，不支持切换");
                    return ;
                }
                if (!dialog.isAdded()) {
                    dialog.show(supportFragmentManager, "configConfirmDialog")
                }
            }
        })

        dialog = ConfirmDialog()
        dialog.setVerifyCodeListener(object : VerificationCodeDialog.VerifyCodeListener {
            override fun onVerifyPass() {
                Log.d(TAG, "onVerifyPass")
                switchServer()
            }
        })
    }

    fun isTestServer(): Boolean {
        return SmartConstans.isTestServer()
    }

    fun switchServer() {
        if(isTest) {
            //当前是测试环境，需要切换到正式环境
            setFlag("default")
        } else {
            //当前是测试环境，需要切换到正式环境
            setFlag("test")
        }
    }

    fun setFlag(value: String) {
        SuperSpUtil.putString(this,  "smartscreen_server_flag", value)
        val flag = SuperSpUtil.getString(this, "smartscreen_server_flag")
        Log.d(TAG, "setFlag : $value, checked : $flag")
        val ret = TextUtils.equals(flag, value)
        if(ret) {
            ToastUtils.getInstance().showGlobalLong("切换成功，需要重新启动应用.")
        }
        HomeIOThread.execute(2000, object: Runnable {
            override fun run() {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        })
    }

    class ConfirmDialog : VerificationCodeDialog() {

        override fun verifyCode(password: String?) {
            if ("0000" == password) {
                setVerifyPass()
            } else {
                setVerifyError("密码错误")
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val title = view.findViewById<TextView>(R.id.title)
            title.setText("请输入密码")
            val tvSubtitle = view.findViewById<TextView>(R.id.subtitle)
            tvSubtitle.setText("切换环境后，需要共享屏APP重新启动生效")
        }
    }
}