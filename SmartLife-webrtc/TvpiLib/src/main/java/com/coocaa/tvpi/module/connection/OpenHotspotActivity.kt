package com.coocaa.tvpi.module.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.widget.CompoundButton
import com.coocaa.publib.base.BaseActivity
import com.coocaa.tvpi.module.base.UnVirtualInputable
import com.coocaa.tvpi.util.StatusBarHelper
import com.coocaa.tvpi.view.CommonTitleBar
import com.coocaa.tvpi.view.CommonTitleBar.ClickPosition
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.activity_open_hotspot.*

class OpenHotspotActivity
    : BaseActivity(), UnVirtualInputable {

    companion object {
        fun starter(context: Context) {
            val intent = Intent(context, OpenHotspotActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_hotspot)
        StatusBarHelper.translucent(this)
        StatusBarHelper.setStatusBarLightMode(this)

        setUnderLine()

        titleBar.setOnClickListener(CommonTitleBar.OnClickListener { position ->
            if (position == ClickPosition.LEFT) {
                finish()
            }
        })

        cbOpenHotspot.setOnCheckedChangeListener(fun(buttonView: CompoundButton, isChecked: Boolean) {
            btNext.isEnabled = isChecked
        })

        tvOpenHotspot.setOnClickListener {
            gotoHotspotSetting()
        }

        btNext.setOnClickListener {
            ConnectNetForDongleActivity.startUserHotpots(this)
            finish()
        }
    }

    private fun setUnderLine(){
        val paint = tvOpenHotspot.paint
        paint.apply {
            flags = Paint.UNDERLINE_TEXT_FLAG
            isAntiAlias = true
        }
    }

    private fun gotoHotspotSetting() {
        val tetherIntent = Intent()
        tetherIntent.addCategory(Intent.CATEGORY_DEFAULT)
        tetherIntent.action = "android.intent.action.MAIN"
        val cn = ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
        tetherIntent.component = cn
        val resolveActivities = packageManager.queryIntentActivities(tetherIntent, 0)
        if (resolveActivities != null && resolveActivities.isNotEmpty()) {
            startActivity(tetherIntent)
        } else {
            val settingIntent = Intent()
            settingIntent.action = Settings.ACTION_SETTINGS
            startActivity(settingIntent)
        }
    }
}