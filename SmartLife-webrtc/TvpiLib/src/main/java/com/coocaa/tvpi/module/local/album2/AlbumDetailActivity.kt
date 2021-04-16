package com.coocaa.tvpi.module.local.album2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coocaa.publib.base.BaseAppletActivity
import com.coocaa.publib.base.GlideApp
import com.coocaa.publib.data.local.ImageData
import com.coocaa.publib.data.local.MediaData
import com.coocaa.publib.data.local.VideoData
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.tvpi.module.local.album2.PreviewActivity.SHOW_ALL
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper
import com.coocaa.tvpi.module.remote.RemoteVirtualInputManager
import com.coocaa.tvpi.util.IntentUtils
import com.coocaa.tvpi.util.dp
import com.coocaa.tvpi.view.decoration.PictureItemDecoration
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.activity_album_detail.*
import java.text.SimpleDateFormat


class AlbumDetailActivity : BaseAppletActivity() {
    private lateinit var albumDetailAdapter: PictureAdapter
    private var albumName: String? = null
    private var pushInAnimation: Animation? = null

    //是否正在添加收藏或者删除收藏
    private var isCollecting: Boolean = false
    private var selectedToCollectData = mutableListOf<MediaData>()

    companion object {
        fun starter(context: FragmentActivity, albumName: String) {
            val intent = Intent(context, AlbumDetailActivity::class.java)
            intent.putExtra("albumName", albumName)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        parseIntent()
        initView()
        initData()
    }

    private fun parseIntent() {
        albumName = IntentUtils.getStringExtra(intent, "albumName")
    }

    private fun initView() {
        mHeaderHandler?.apply { 
            setTitle(albumName)
            setBackgroundColor(Color.parseColor("#f4f4f4"))
        }

        albumDetailAdapter = PictureAdapter(this)
        recyclerview.apply {
            addItemDecoration(PictureItemDecoration(4, 1.dp, 1.dp))
            layoutManager = GridLayoutManager(this@AlbumDetailActivity, 4)
            adapter = albumDetailAdapter
        }

        albumDetailAdapter.setItemClickListener(object : PictureAdapter.PictureItemClickListener {
            override fun onItemCheckChange(isCheck: Boolean, mediaData: MediaData) {
                if(isCheck){
                    selectedToCollectData.add(mediaData)
                }else{
                    selectedToCollectData.remove(mediaData)
                }
                tvSelectedCount.text = Html.fromHtml("已选择<font color=\"#F86239\"> ${selectedToCollectData.size} </font>项")
                btAddCollect.isEnabled = selectedToCollectData.isNotEmpty()
            }

            override fun onItemClick(position: Int) {
                PreviewActivityW7.start(this@AlbumDetailActivity, albumName, position, SHOW_ALL)
            }
        })

        btCollectSwitch.setOnClickListener {
            isCollecting = !isCollecting
            if (isCollecting) {
                doCollecting()
            } else {
                cancelCollecting()
            }
        }

        btAddCollect.setOnClickListener {
            //重置选中状态(因为收藏和照片列表中的选中状态共用的一个状态)
            selectedToCollectData.map { it.isCheck = false }
            LocalMediaHelper.getInstance().collectMediaData(this@AlbumDetailActivity, selectedToCollectData)
            cancelCollecting()
            ToastUtils.getInstance().showGlobalShort("已添加收藏")
        }
    }

    private fun initData() {
        val allMediaDataMap = LocalMediaHelper.getInstance().allMediaDataMap
        val pictureList = allMediaDataMap?.get(albumName)
        albumDetailAdapter.setList(pictureList)
    }

    private fun doCollecting() {
        tvSelectedCount.visibility = View.VISIBLE
        btCollectSwitch.setBackgroundResource(R.drawable.icon_picture_cancel_switch_collect)
        RemoteVirtualInputManager.hideFloatViewToActivity(this)
        if (pushInAnimation == null) {
            pushInAnimation = AnimationUtils.loadAnimation(this@AlbumDetailActivity, R.anim.push_bottom_in)
        }
        addCollectOperateLayout.apply {
            clearAnimation()
            visibility = View.VISIBLE
            startAnimation(pushInAnimation)
        }
        albumDetailAdapter.showCheckbox(true)
    }

    private fun cancelCollecting() {
        selectedToCollectData.map { it.isCheck = false }
        tvSelectedCount.text = Html.fromHtml("已选择<font color=\"#F86239\">0</font>项")
        tvSelectedCount.visibility = View.GONE
        btCollectSwitch.setBackgroundResource(R.drawable.icon_picture_switch_collect)
        addCollectOperateLayout.apply {
            clearAnimation()
            visibility = View.GONE
        }
        RemoteVirtualInputManager.showFloatViewWithAnimToActivity(this)
        albumDetailAdapter.showCheckbox(false)
    }


    class PictureAdapter(
            private val activity: Activity
    ) : BaseQuickAdapter<MediaData, BaseViewHolder>(R.layout.item_picture) {
        private var showCheckbox = false
        private var listener: PictureItemClickListener? = null

        fun showCheckbox(show: Boolean) {
            if (showCheckbox != show) {
                showCheckbox = show
                notifyDataSetChanged()
            }
        }

        fun setItemClickListener(listener: PictureItemClickListener?) {
            this.listener = listener
        }

        @SuppressLint("SimpleDateFormat")
        override fun convert(holder: BaseViewHolder, item: MediaData) {
            Log.d("CollectionAdapter", "convert: $item")
            val ivCover: ImageView = holder.getView(R.id.ivCover)
            val tvDuring: TextView = holder.getView(R.id.tvVideoDuring)
            val ivVideoFlag: ImageView = holder.getView(R.id.ivVideoFlag)
            val ivSelect: ImageView = holder.getView(R.id.cbSelect)
            val bottomMaskView: View = holder.getView(R.id.bottomMask)
            val totalMaskView: View = holder.getView(R.id.totalMask)
            val imageSize = DimensUtils.getDeviceWidth(context) / 4f

            when (item) {
                is ImageData -> {
                    GlideApp.with(activity)
                            .asBitmap()
                            .load(item.url)
                            .override(imageSize.toInt())
                            .into(ivCover)
                    tvDuring.visibility = View.GONE
                    ivVideoFlag.visibility = View.GONE
                    bottomMaskView.visibility = View.GONE
                }
                is VideoData -> {
                    GlideApp.with(activity)
                            .load(item.thumbnailPath)
                            .override(imageSize.toInt(), imageSize.toInt())
                            .into(ivCover)
                    tvDuring.apply {
                        visibility = View.VISIBLE
                        text = SimpleDateFormat("mm:ss").format(item.duration)
                    }
                    ivVideoFlag.visibility = View.VISIBLE
                    bottomMaskView.visibility = View.VISIBLE
                }
            }

            ivSelect.apply {
                visibility = if (showCheckbox) View.VISIBLE else View.GONE
                setBackgroundResource(if (showCheckbox && item.isCheck) R.drawable.icon_picture_checked else R.drawable.icon_picture_uncheck)
            }

            totalMaskView.visibility = if (showCheckbox && item.isCheck) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                if (showCheckbox) {
                    item.isCheck = !item.isCheck
                    notifyItemChanged(getItemPosition(item))
                    listener?.onItemCheckChange(item.isCheck, item)
                } else {
                    listener?.onItemClick(getItemPosition(item))
                }
            }
        }

        interface PictureItemClickListener {
            fun onItemCheckChange(isCheck: Boolean, mediaData: MediaData)

            fun onItemClick(position: Int)
        }
    }
}