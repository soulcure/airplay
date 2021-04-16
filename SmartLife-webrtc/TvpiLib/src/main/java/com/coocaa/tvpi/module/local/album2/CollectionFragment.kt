package com.coocaa.tvpi.module.local.album2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coocaa.publib.base.GlideApp
import com.coocaa.publib.data.local.ImageData
import com.coocaa.publib.data.local.MediaData
import com.coocaa.publib.data.local.VideoData
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.tvpi.base.BaseFragment
import com.coocaa.tvpi.module.local.album2.PreviewActivity.SHOW_COLLECT
import com.coocaa.tvpi.module.local.album2.viewmodel.PictureShareViewModel
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper
import com.coocaa.tvpi.module.local.view.LocalResStatesView
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.fragment_picture_collection.*
import java.text.SimpleDateFormat

class CollectionFragment : BaseFragment() {
    private var shareViewModel: PictureShareViewModel? = null
    private lateinit var collectionAdapter: CollectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_picture_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            shareViewModel = ViewModelProviders.of(it).get(PictureShareViewModel::class.java)
            initView()
            observerIsCollecting()
            observerIsSelectAll()
            observerNotifyChange()
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    fun initView() {
        collectionAdapter = CollectionAdapter()
        recyclerview.apply {
            adapter = collectionAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
    }

    fun initData() {
        val collectedMediaData = LocalMediaHelper.getInstance().getCollectedMediaData(activity!!)
        if (collectedMediaData == null || collectedMediaData.isEmpty()) {
            stateView.setViewLoadState(LocalResStatesView.STATE_NO_DATA, "尚未收藏任何照片")
        } else {
            collectionAdapter.setList(collectedMediaData)
            stateView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH)
        }
    }

    //是否正在编辑收藏
    private fun observerIsCollecting() {
        shareViewModel?.isCollecting()?.observe(activity!!, object : Observer<Boolean> {
            override fun onChanged(collcting: Boolean) {
                collectionAdapter.showCheckbox(collcting)
                //取消编辑后重置数据的选中标记和选中数量
                if (!collcting) {
                    shareViewModel?.clearSelectedToCollectData()
                    collectionAdapter.data.map { it.isCheck = false }
                }
            }
        })
    }

    //是否全选或取消全选
    private fun observerIsSelectAll() {
        shareViewModel?.isSelectAllData()?.observe(activity!!, object : Observer<Boolean> {
            override fun onChanged(selectedAll: Boolean) {
                collectionAdapter.data.map { it.isCheck = selectedAll }
                collectionAdapter.notifyDataSetChanged()
                if (selectedAll) {
                    shareViewModel?.addSelectedToCollectData(collectionAdapter.data)
                } else {
                    shareViewModel?.clearSelectedToCollectData()
                }
            }
        })
    }

    //是否需要刷新UI（添加、删除收藏）
    private fun observerNotifyChange() {
        shareViewModel?.isNotifyCollectChange()?.observe(activity!!, object : Observer<Boolean> {
            override fun onChanged(notifyChange: Boolean?) {
                if (notifyChange != null && notifyChange) {
                    initData()
                }
            }
        })
    }

    private inner class CollectionAdapter : BaseQuickAdapter<MediaData, BaseViewHolder>(R.layout.item_picture) {
        private var showCheckbox = false

        fun showCheckbox(show: Boolean) {
            if (showCheckbox != show) {
                showCheckbox = show
                notifyDataSetChanged()
            }
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
                    GlideApp.with(this@CollectionFragment)
                            .asBitmap()
                            .load(item.url)
                            .override(imageSize.toInt())
                            .into(ivCover)
                    tvDuring.visibility = View.GONE
                    ivVideoFlag.visibility = View.GONE
                    bottomMaskView.visibility = View.GONE
                }
                is VideoData -> {
                    GlideApp.with(this@CollectionFragment)
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
                    if (item.isCheck) {
                        shareViewModel?.addSelectedToCollectData(item)
                    } else {
                        shareViewModel?.removeSelectedToCollectData(item)
                    }
                } else {
                    if (activity != null) {
                        PreviewActivityW7.start(activity, LocalMediaHelper.MAIN_ALBUM_NAME, getItemPosition(item), SHOW_COLLECT)
                    }
                }
            }
        }
    }
}

