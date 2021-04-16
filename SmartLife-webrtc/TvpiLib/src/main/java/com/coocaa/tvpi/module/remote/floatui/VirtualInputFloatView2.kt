package com.coocaa.tvpi.module.remote.floatui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.coocaa.publib.base.GlideApp
import com.coocaa.publib.utils.SpUtil
import com.coocaa.smartscreen.businessstate.`object`.BusinessState
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean
import com.coocaa.smartsdk.SmartApi
import com.coocaa.swaiotos.virtualinput.VirtualInputStarter
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateManager
import com.coocaa.tvpi.module.io.HomeIOThread
import com.coocaa.tvpi.module.io.HomeUIThread
import com.coocaa.tvpi.util.TvpiClickUtil
import com.coocaa.tvpilib.R
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @Author: yuzhan
 */
open class VirtualInputFloatView2(context: Context, bottom: Int) : FrameLayout(context!!) {

    private var params: LayoutParams
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    protected  var mContext: Context
    protected var containerView: FrameLayout
    protected var dotView: View
    private var title: TextView
    private var subTitle: TextView
    private var icon: ImageView

    protected var tipView: View
    protected var tipImageView: ImageView
    private var contentOneDefaultRl: RelativeLayout
    private var contentTwoLoadingRl: RelativeLayout
    private var loadingIcon: LottieAnimationView
    private var loadingFinishIcon: ImageView
    private var loadingTitle: TextView

    private val emptyTitle = "共享屏无内容播放"
    private val emptySubTitle = "投送内容后，点击这里可以遥控"
    private var mUserID: String? = null
    private var pushPageType: String? = null
    private var loadingResult: Boolean = false

    protected var TAG = "FloatVI2"
    val KEY_HAS_SHOWN_FIRST_TIPS_DOC = "floatui_first_tips_doc"
    val KEY_HAS_SHOWN_FIRST_TIPS_ALBUM = "floatui_first_tips_album"
    val KEY_HAS_SHOWN_FIRST_TIPS_LIVE = "floatui_first_tips_live"

    init {
        mContext = context
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        clipChildren = false
        if (screenWidth == 0 || screenHeight == 0) {
            screenWidth = context.applicationContext!!.resources.displayMetrics.widthPixels
            screenHeight = context.applicationContext!!.resources.displayMetrics.heightPixels
            Log.d(TAG, "22 screenWidth=$screenWidth, screenHeight=$screenHeight")
        }

        containerView = FrameLayout(context)
        params = LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM)
        if (bottom > 0) {
            params.bottomMargin = bottom
        }
        containerView.layoutParams = params
        addView(containerView)

        tipView = LayoutInflater.from(context).inflate(R.layout.floatui_first_tips_layout, null)
        tipView.isClickable = true
        tipView.visibility = View.INVISIBLE
        tipImageView = tipView.findViewById(R.id.floatui_first_tips_img)
        containerView.addView(tipView)

        dotView = LayoutInflater.from(context).inflate(R.layout.remote_floatui_layout, null)
        var dotParams = LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        dotParams.gravity = Gravity.BOTTOM
        dotView.layoutParams = dotParams
        dotView.isClickable = true
        //dotView.setBackgroundResource(R.color.color_white_a96)
        containerView.addView(dotView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 1f
        }

        dotView.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                if (tipView.visibility == View.VISIBLE) {
                    tipImageView.clearAnimation()
                    containerView.removeView(tipView)
                    if ("album".equals(pushPageType)) {
                        SpUtil.putBoolean(context, KEY_HAS_SHOWN_FIRST_TIPS_ALBUM, true)
                    } else if ("document".equals(pushPageType)) {
                        SpUtil.putBoolean(context, KEY_HAS_SHOWN_FIRST_TIPS_DOC, true)
                    }else if("live".equals(pushPageType)) {
                        SpUtil.putBoolean(context, KEY_HAS_SHOWN_FIRST_TIPS_LIVE, true)
                    }
                }
                startRemoteActivity()
            }
        })

        title = dotView.findViewById<TextView>(R.id.tv_title)
        subTitle = dotView.findViewById<TextView>(R.id.tv_subtitle)
        icon = dotView.findViewById<ImageView>(R.id.img_type_icon)

        contentOneDefaultRl = dotView.findViewById(R.id.content_one_default_rl)
        contentTwoLoadingRl = dotView.findViewById(R.id.content_two_loading_rl)
        loadingIcon = dotView.findViewById<LottieAnimationView>(R.id.img_loading_icon)
        loadingFinishIcon = dotView.findViewById<ImageView>(R.id.img_load_finish)
        loadingTitle = dotView.findViewById<TextView>(R.id.tv_loading_title)

        loadingIcon.setAnimation("virtual_floatui_loading.json")
        loadingIcon.setRepeatCount(INFINITE)
    }

    open fun onShow() {
        var hasDevice = SmartApi.hasDevice()
        Log.d(TAG, "onShow: hasDevice: $hasDevice")
        if(hasDevice) {
            dotView.visibility = View.VISIBLE
        }else {
            dotView.visibility = View.INVISIBLE
        }

        Log.d(TAG, "onShow: isLoading: " + FloatVIStateManager.getIsLoading())
        if(FloatVIStateManager.getIsLoading()) {
            refreshLoadingUI()
        }else {
            contentOneDefaultRl.visibility = View.VISIBLE
            contentTwoLoadingRl.visibility = View.INVISIBLE
        }
    }

    open fun onHide() {
        if (loadingIcon != null)
            loadingIcon.cancelAnimation()
        HomeUIThread.removeTask(overTimeRunnable)
    }

    fun onStateChanged(businessState: BusinessState?) {
        Log.d(TAG, "onStateChanged: $businessState")
        if(!SmartApi.isDeviceConnect() || businessState == null){
            title.text = emptyTitle
            subTitle.text = emptySubTitle
            icon.setImageResource(R.drawable.floatui_default_type_icon)
            return
        }
        
        val state = businessState!!
        if (TextUtils.isEmpty(state.id)) {
            // 当id为null时，表示是旧版业务数据,需要根据type去解析配置文件
            if (!TextUtils.isEmpty(state.type)) {
                refreshStateUIOnOldDevice(FloatVIStateManager.getSceneConfigBean(state.type), state)
            }
        } else {
            //当id不是null时，表示是新版业务数据,需要根据id去解析配置文件
            refreshStateUI(FloatVIStateManager.getSceneConfigBean(state.id), state)
        }
    }

    //低版本dongle设备
    private fun refreshStateUIOnOldDevice(bean: SceneConfigBean?, state: BusinessState?) {
        Log.d(TAG, "refreshStateUIOnOldDevice : $bean")
        var isInHomeApp = false
        val isApp = "APP" == state?.type?.toUpperCase(Locale.US)
        if (isApp) {
            isInHomeApp = state?.values?.contains("com.coocaa.dongle.launcher") ?: false
        }
        if (isInHomeApp) {
            title.text = emptyTitle
            subTitle.text = emptySubTitle
            icon.setImageResource(R.drawable.floatui_default_type_icon)
        } else {
            if (isApp) {
                return
            }
            if (bean == null || TextUtils.isEmpty(bean.appletName)) {
                title.text = emptyTitle
                subTitle.text = emptySubTitle
                icon.setImageResource(R.drawable.floatui_default_type_icon)
            } else {
                title.text = bean.appletName
                if (TextUtils.isEmpty(bean.subTitle)) {
                    subTitle.visibility = GONE
                } else {
                    subTitle.visibility = VISIBLE
                    subTitle.text = bean.subTitle
                }
                if (!TextUtils.isEmpty(bean.appletIcon)) {
                    if (context != null) {
                        GlideApp.with(context).load(bean.appletIcon).into(icon)
                    }
                } else {
                    icon.setImageResource(R.drawable.floatui_default_type_icon)
                }
            }
        }
    }

    private fun refreshStateUI(bean: SceneConfigBean?, state: BusinessState?) {
        Log.d(TAG, "refreshStateUI : $bean")
        if (bean == null || state == null) {
            subTitle.visibility = VISIBLE
            title.text = emptyTitle
            subTitle.text = emptySubTitle
            icon.setImageResource(R.drawable.floatui_default_type_icon)
            return
        }
        if (bean != null && state != null) {
            if (!TextUtils.isEmpty(bean.appletIcon)) {
                if (context != null) {
                    GlideApp.with(context).load(bean.appletIcon).into(icon)
                }
            } else {
                icon.setImageResource(R.drawable.floatui_default_type_icon)
            }
            getUserId()
            updateTitle(bean, state)
            updateSubtitle(bean, state)
        }
    }

    fun updateTitle(bean: SceneConfigBean, state: BusinessState) {
        var titleFormat: String? = bean.titleFormat
        if(!TextUtils.isEmpty(titleFormat)) {
            if (titleFormat!!.contains("%u")) {
                titleFormat = parseUser(titleFormat, state)
            }
            if (titleFormat!!.contains("%t")) {
                titleFormat = parseTitle(titleFormat, state)
            }
        }
        title.setText(titleFormat)
    }

    fun updateSubtitle(bean: SceneConfigBean, state: BusinessState) {
        val sceneSubtitle: String? = bean.subTitle
        if (TextUtils.isEmpty(sceneSubtitle)) {
            subTitle.setVisibility(GONE)
        } else {
            subTitle.setVisibility(VISIBLE)
            subTitle.setText(parseTitle(sceneSubtitle!!, state))
        }
    }

    fun parseUser(targetString: String, state: BusinessState): String? {
        var titleFormat = targetString
        val userID: String? = if(state.owner == null) null else state.owner.userID
        val nickName: String? = if(state.owner == null) null else state.owner.nickName
        val mobile: String? = if(state.owner == null) null else state.owner.mobile
        Log.d(TAG, "parseUser: statebean_id: $userID")
        Log.d(TAG, "parseUser: userinfo_id: $mUserID")

        //userId相等显示[我]，不相等的情况下能拿到昵称优先显示昵称，拿不到昵称显示手机号
        if (!TextUtils.isEmpty(titleFormat) && titleFormat.contains("%u")) {
            titleFormat = if (!TextUtils.isEmpty(userID)) {
                if (userID == mUserID) {
                    titleFormat.replace("%u", "我")
                } else if (!TextUtils.isEmpty(nickName)) {
                    titleFormat.replace("%u", "$nickName ")
                } else if (!TextUtils.isEmpty(mobile)) {
                    titleFormat.replace("%u",
                            mobile?.substring(0, 3) + "****" + mobile?.substring(7) + " ")
                } else {
                    titleFormat.replace("u%", "")
                }
            } else {
                titleFormat.replace("u%", "")
            }
        }
        return titleFormat
    }

    fun parseTitle(targetString: String, state: BusinessState): String? {
        var titleFormat = targetString
        if (!TextUtils.isEmpty(titleFormat) && titleFormat.contains("%t")) {
            val values: String? = state.values
            try {
                val jsonObject = JSONObject(values)
                val title = jsonObject.optString("title")
                Log.d(TAG, "parseTitle: $title")
                titleFormat = if (!TextUtils.isEmpty(title)) {
                    titleFormat.replace("%t", title)
                } else {
                    titleFormat.replace("%t", "")
                }
            } catch (e: JSONException) {
                titleFormat = titleFormat.replace("%t", "")
                e.printStackTrace()
            }
        }
        return titleFormat
    }

    fun startLoading(pushPageType: String) {
        this.pushPageType = pushPageType
        if(pushPageType.equals("live")) {
            afterPush()
        }else {
            contentOneDefaultRl.visibility = View.INVISIBLE
            contentTwoLoadingRl.visibility = View.VISIBLE
            loadingFinishIcon.visibility = View.INVISIBLE
            loadingIcon.visibility = View.VISIBLE
            loadingTitle.setText("内容投送中...")
            startLoadingAnim()
            HomeUIThread.execute(11000, overTimeRunnable)
        }
    }

    fun refreshLoadingUI() {
        contentOneDefaultRl.visibility = View.INVISIBLE
        contentTwoLoadingRl.visibility = View.VISIBLE
        loadingFinishIcon.visibility = View.INVISIBLE
        loadingIcon.visibility = View.VISIBLE
        if (!loadingIcon.isAnimating) {
            loadingIcon.post {
                loadingIcon.playAnimation()
            }
        }
        loadingTitle.setText("内容投送中...")
    }

    fun refreshLoadingUI(progress: Int) {
        Log.d(TAG, "refreshLoadingUI: " + progress)
        if (progress >= 0 && progress <= 100) {
            HomeUIThread.removeTask(overTimeRunnable)
            if("album".equals(this.pushPageType)) {
                loadingTitle.text = "内容投送中..."
            }else {
                loadingTitle.text = "内容投送中..." + progress + "%"
            }
        }
    }

    fun showLoadingResultUI(result: Boolean) {
        Log.d(TAG, "showLoadingResultUI: load finish result: " + result)
        HomeUIThread.removeTask(overTimeRunnable)
        loadingIcon.cancelAnimation()
        loadingIcon.visibility = View.INVISIBLE
        loadingFinishIcon.visibility = View.VISIBLE
        loadingResult = result
        if (result) {
            loadingFinishIcon.setImageResource(R.drawable.floatui_load_ok)
            loadingTitle.setText("内容投送成功")
        } else {
            loadingFinishIcon.setImageResource(R.drawable.floatui_load_failed)
            loadingTitle.setText("内容投送超时，请重试")
        }
        afterPush()

        HomeUIThread.execute(2500, showDefaultUIRunnable)
    }

    open fun onDeviceConnect() {
        Log.d(TAG, "float view receive : onDeviceConnect")
    }

    open fun onDeviceDisconnect() {
        Log.d(TAG, "float view receive : onDeviceDisconnect")
        HomeUIThread.execute(Runnable {
            title.text = emptyTitle
            subTitle.text = emptySubTitle
        })
    }

    open fun onStateInit() {
        var hasDevice = SmartApi.hasDevice()
        Log.d(TAG, "onStateInit: hasDevice: $hasDevice")
        if(hasDevice) {
            dotView.visibility = View.VISIBLE
        }else {
            dotView.visibility = View.INVISIBLE
        }
    }

    fun startRemoteActivity() {
        val isConnected = SmartApi.isDeviceConnect()
        val connectedDevice = SmartApi.getConnectDeviceInfo()
        Log.d("SmartApi", "isConnected=$isConnected, device=$connectedDevice")
        if (isConnected && connectedDevice != null) {
            HomeIOThread.execute {
//                if (!SmartApi.isSameWifi()) {
//                    WifiConnectTipActivity.start(context)
//                } else {
//                    TvpiClickUtil.onClick(context, "np://com.coocaa.smart.floatvirtualinput/index?from=floatui")
//                }
                //需求调整，允许非同一wifi进遥控页面
                TvpiClickUtil.onClick(context, "np://com.coocaa.smart.floatvirtualinput/index?from=floatui")
            }
        } else {
            startConnectDevice()
        }
    }

    protected open fun startConnectDevice() {
        SmartApi.startConnectDevice()
    }

    fun getUserId() {
        val mCoocaaUserInfo = SmartApi.getUserInfo()
        Log.d(TAG, "getUserId: $mCoocaaUserInfo")
        if (mCoocaaUserInfo != null) {
            mUserID = mCoocaaUserInfo.open_id
        }
    }

    fun startLoadingAnim() {
        val defaultContent_AnimOut: AnimationSet = AnimationUtils.loadAnimation(context, R.anim.floatui_default_content_out) as AnimationSet
        val loadingContent_AnimIn: AnimationSet = AnimationUtils.loadAnimation(context, R.anim.floatui_loading_content_in) as AnimationSet

        defaultContent_AnimOut.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                if (!loadingIcon.isAnimating) {
                    loadingIcon.post {
                        loadingIcon.playAnimation()
                    }
                }
            }

            override fun onAnimationEnd(animation: Animation?) {
                contentOneDefaultRl.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        contentOneDefaultRl.startAnimation(defaultContent_AnimOut)
        contentTwoLoadingRl.startAnimation(loadingContent_AnimIn)
    }

    private val showDefaultUIRunnable = object : Runnable {
        override fun run() {
            stopLoadingAnim()
        }
    }

    fun stopLoadingAnim() {
        val defaultContent_AnimIn: Animation = AnimationUtils.loadAnimation(context, R.anim.floatui_default_content_in)
        val loadingContent_AnimOut: Animation = AnimationUtils.loadAnimation(context, R.anim.floatui_loading_content_out)

        defaultContent_AnimIn.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                contentOneDefaultRl.visibility = View.VISIBLE
                contentTwoLoadingRl.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                contentOneDefaultRl.visibility = View.VISIBLE
                contentTwoLoadingRl.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        contentOneDefaultRl.startAnimation(defaultContent_AnimIn)
        contentTwoLoadingRl.startAnimation(loadingContent_AnimOut)
    }

    fun afterPush() {
        if("document".equals(pushPageType) && SpUtil.getBoolean(mContext, KEY_HAS_SHOWN_FIRST_TIPS_DOC, false) && loadingResult){
            VirtualInputStarter.show(context, true)
            return
        }
        if ("document".equals(pushPageType) && !SpUtil.getBoolean(mContext, KEY_HAS_SHOWN_FIRST_TIPS_DOC, false)) {
            tipView.visibility = View.VISIBLE
            startTipsAnim()
        } else if ("album".equals(pushPageType) && !SpUtil.getBoolean(mContext, KEY_HAS_SHOWN_FIRST_TIPS_ALBUM, false)) {
            tipView.visibility = View.VISIBLE
            startTipsAnim()
        }else if("live".equals(pushPageType) && !SpUtil.getBoolean(mContext, KEY_HAS_SHOWN_FIRST_TIPS_LIVE, false)) {
            tipView.visibility = View.VISIBLE
            startTipsAnim()
        }
    }

    fun startTipsAnim() {
        val tipImagAnimation = ObjectAnimator.ofFloat(tipImageView, "translationY", 0f, 10f, 0f)
        tipImagAnimation.duration = 1000
        tipImagAnimation.repeatCount = Animation.INFINITE
        tipImagAnimation.start()
    }

    private val overTimeRunnable = object : Runnable {
        override fun run() {
            stopLoadingAnim()
        }
    }

}