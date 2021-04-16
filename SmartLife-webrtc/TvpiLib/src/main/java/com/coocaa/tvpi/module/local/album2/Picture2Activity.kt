package com.coocaa.tvpi.module.local.album2

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.coocaa.publib.base.BaseAppletActivity
import com.coocaa.publib.data.local.MediaData
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.tvpi.module.local.album2.viewmodel.PictureShareViewModel
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper
import com.coocaa.tvpi.module.remote.RemoteVirtualInputManager
import com.coocaa.tvpi.util.MagicIndicatorUtils
import com.coocaa.tvpi.util.dp
import com.coocaa.tvpilib.R
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_pirture_new.*
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import swaiotos.runtime.base.style.AppletTitleStyle
import kotlin.math.abs

class Picture2Activity : BaseAppletActivity() {
    companion object {
        const val POSITION_TAB_PICTURE = 0   //照片
        const val POSITION_TAB_ALBUM = 1     //相册
    }

    private lateinit var shareViewModel: PictureShareViewModel
    private var pushInAnimation: Animation? = null
    private var selectedToCollectData: List<MediaData>? = null
    private var curPosition: Int = 0

    //是否正在添加收藏或者删除收藏
    private var isCollecting: Boolean = false

    //是否已经全部选中收藏数据
    private var isSelectedAllCollectedData: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pirture_new)
        shareViewModel = ViewModelProviders.of(this).get(PictureShareViewModel::class.java)
        initView()
        observerSelectedToCollectData()
    }

    private fun initView() {
        mHeaderHandler?.apply {
            setTitle("")
            setBackgroundColor(Color.parseColor("#f4f4f4"))
            setTitleStyle(AppletTitleStyle().setFakeBold(true))
        }

        val fragments = listOf<Fragment>(
            PictureListFragment(),
            AlbumListFragment()
        )

        viewpager2.adapter = ViewPagerAdapter(
            supportFragmentManager,
            lifecycle,
            fragments
        )

        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = PictureNavigatorAdapter(fragments)
        indicator.navigator = commonNavigator
        MagicIndicatorUtils.bindViewPager2(indicator, viewpager2)

        appbarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                mHeaderHandler?.setTitle(if (abs(verticalOffset) >= (appBarLayout!!.totalScrollRange - 5.dp)) "相册投电视" else "")
            }
        })

        viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                curPosition = position
                /*if (position == POSITION_TAB_ALBUM) {
                    btCollectSwitch.visibility = View.GONE
                } else if (position == POSITION_TAB_COLLECTION) {
                    val collectedMediaData = LocalMediaHelper.getInstance().getCollectedMediaData(this@Picture2Activity)
                    if (collectedMediaData == null || collectedMediaData.isEmpty()) {
                        btCollectSwitch.visibility = View.GONE
                    } else {
                        btCollectSwitch.visibility = View.VISIBLE
                    }
                } else {
                    btCollectSwitch.visibility = View.VISIBLE
                }*/
            }
        })

        /*btCollectSwitch.setOnClickListener {
            isCollecting = !isCollecting
            if (isCollecting) {
                doCollecting()
            } else {
                cancelCollecting()
            }
        }*/

        btAddCollect.setOnClickListener {
            //重置选中状态(因为收藏和照片列表中的选中状态共用的一个状态)
            selectedToCollectData?.map { it.isCheck = false }
            LocalMediaHelper.getInstance().collectMediaData(this@Picture2Activity, selectedToCollectData)
            cancelCollecting()
            shareViewModel.setNotifyCollectChange(true)
            ToastUtils.getInstance().showGlobalShort("已添加收藏")
        }

        btDeleteCollect.setOnClickListener {
            LocalMediaHelper.getInstance().removeMediaData(this@Picture2Activity, selectedToCollectData)
            cancelCollecting()
            shareViewModel.setNotifyCollectChange(true)
            ToastUtils.getInstance().showGlobalShort("已移除收藏")
        }

        btSelectOrUnSelectAll.setOnClickListener {
            shareViewModel.setSelectAllData(!isSelectedAllCollectedData)
        }
    }


    //观察正在添加收藏的item
    private fun observerSelectedToCollectData() {
        /* shareViewModel.getSelectedToCollectData().observe(this, object : Observer<List<MediaData>> {
             override fun onChanged(list: List<MediaData>) {
                 selectedToCollectData = list
                 tvSelectedCount.text = Html.fromHtml("已选择<font color=\"#F86239\"> ${list.size} </font>项")
                 if (curPosition == POSITION_TAB_PICTURE) {
                     btAddCollect.isEnabled = list.isNotEmpty()
                 } else if (curPosition == POSITION_TAB_COLLECTION) {
                     btDeleteCollect.isEnabled = list.isNotEmpty()
                     val collectedMediaData = LocalMediaHelper.getInstance().getCollectedMediaData(this@Picture2Activity)
                     isSelectedAllCollectedData = collectedMediaData != null && collectedMediaData.size == list.size
                     btSelectOrUnSelectAll.text = if (isSelectedAllCollectedData) "取消全选" else "全选"
                 }
             }
         })*/
    }

    private fun doCollecting() {
        /*viewpager2.isUserInputEnabled = false
        tvSelectedCount.visibility = View.VISIBLE
        btCollectSwitch.setBackgroundResource(R.drawable.icon_picture_cancel_switch_collect)
        RemoteVirtualInputManager.hideFloatViewToActivity(this)
        if(pushInAnimation == null) {
            pushInAnimation = AnimationUtils.loadAnimation(this@Picture2Activity, R.anim.push_bottom_in)
        }
        if (curPosition == POSITION_TAB_PICTURE) {
            addCollectOperateLayout.apply {
                clearAnimation()
                visibility = View.VISIBLE
                startAnimation(pushInAnimation)
            }
        } else if (curPosition == POSITION_TAB_COLLECTION) {
            deleteCollectOperateLayout.apply {
                clearAnimation()
                visibility = View.VISIBLE
                startAnimation(pushInAnimation)
            }
        }
        shareViewModel.setCollecting(true)*/
    }

    private fun cancelCollecting() {
        /*viewpager2.isUserInputEnabled = true
        tvSelectedCount.visibility = View.GONE
        btCollectSwitch.setBackgroundResource(R.drawable.icon_picture_switch_collect)
        addCollectOperateLayout.apply {
            clearAnimation()
            visibility = View.GONE
        }
        deleteCollectOperateLayout.apply {
            clearAnimation()
            visibility = View.GONE
        }
        RemoteVirtualInputManager.showFloatViewWithAnimToActivity(this)
        shareViewModel.setCollecting(false)*/
    }

    private inner class PictureNavigatorAdapter(
        private val fragmentList: List<Fragment>
    ) : CommonNavigatorAdapter() {
        override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
            return ColorTransitionPagerTitleView(context).apply {
                text = when (index) {
                    POSITION_TAB_PICTURE -> "全部照片"
                    POSITION_TAB_ALBUM -> "手机相册"
                    else -> "收藏"
                }
                normalColor = Color.parseColor("#99000000")
                selectedColor = Color.parseColor("#ff188cff")
                typeface = Typeface.DEFAULT_BOLD
                width = 92.dp
                height = 36.dp
                setOnClickListener {
                    viewpager2.currentItem = index
                }
            }
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getIndicator(context: Context?): IPagerIndicator {
            return LinePagerIndicator(context).apply {
                lineHeight = 36f.dp
                roundRadius = 18f.dp
                elevation = 5f.dp
                setColors(Color.parseColor("#ffffff"))
            }
        }
    }

    private class ViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val fragmentList: List<Fragment>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}


