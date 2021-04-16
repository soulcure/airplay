package com.coocaa.tvpi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.coocaa.smartscreen.R
import com.coocaa.tvpi.module.connection.ScanActivity2
import kotlinx.android.synthetic.main.layout_mainpager_bottom_nav.view.*

class BottomNavView(context: Context,
                    attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private var listener: OnNavChangeListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_mainpager_bottom_nav, this, true)
        selectMainPagerTab()

        mainPagerLayout.setOnClickListener {
            selectMainPagerTab()
            listener?.onNavChange(0)
        }

        ivScan.setOnClickListener {
            ScanActivity2.start(context)
        }

        discoverLayout.setOnClickListener {
            selectDiscoverTab()
            listener?.onNavChange(1)
        }
    }

    private fun selectMainPagerTab(){
        ivMainPager.isSelected = true
        tvMainPager.isSelected = true
        ivDiscover.isSelected = false
        tvDiscover.isSelected = false
    }

    private fun selectDiscoverTab(){
        ivMainPager.isSelected = false
        tvMainPager.isSelected = false
        ivDiscover.isSelected = true
        tvDiscover.isSelected = true
    }

    fun setOnNavChangeListener(onNavChangeListener: OnNavChangeListener) {
        this.listener = onNavChangeListener
    }

    interface OnNavChangeListener {
        fun onNavChange(index: Int)
    }
}