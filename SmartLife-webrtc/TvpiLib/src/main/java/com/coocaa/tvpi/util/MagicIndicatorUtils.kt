package com.coocaa.tvpi.util

import androidx.viewpager2.widget.ViewPager2
import net.lucode.hackware.magicindicator.MagicIndicator

object MagicIndicatorUtils {
    fun bindViewPager2(magicIndicator: MagicIndicator, viewPager2: ViewPager2) {
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                magicIndicator.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                magicIndicator.onPageSelected(position)
            }
        })
    }
}