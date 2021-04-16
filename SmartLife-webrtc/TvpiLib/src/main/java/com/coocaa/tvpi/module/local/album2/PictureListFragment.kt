package com.coocaa.tvpi.module.local.album2

import android.Manifest
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
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.entity.JSectionEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coocaa.publib.base.GlideApp
import com.coocaa.publib.data.local.ImageData
import com.coocaa.publib.data.local.MediaData
import com.coocaa.publib.data.local.VideoData
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.tvpi.base.BaseFragment
import com.coocaa.tvpi.event.LocalAlbumLoadEvent
import com.coocaa.tvpi.module.io.HomeUIThread
import com.coocaa.tvpi.module.local.album2.PreviewActivity.SHOW_ALL
import com.coocaa.tvpi.module.local.album2.viewmodel.PictureShareViewModel
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper.MAIN_ALBUM_NAME
import com.coocaa.tvpi.module.local.view.LocalResStatesView
import com.coocaa.tvpi.util.DateUtils
import com.coocaa.tvpi.util.permission.PermissionListener
import com.coocaa.tvpi.util.permission.PermissionsUtil
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.fragment_picture_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.sql.Date
import java.text.SimpleDateFormat


class PictureListFragment : BaseFragment() {
    private lateinit var sectionAdapter: PictureSectionAdapter
    private var shareViewModel: PictureShareViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_picture_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            EventBus.getDefault().register(this)
            shareViewModel = ViewModelProviders.of(it).get(PictureShareViewModel::class.java)
            initView()
            initData()
            observerIsCollecting()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    private fun initView() {
        sectionAdapter = PictureSectionAdapter()
        recyclerview.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = sectionAdapter
        }
    }

    private fun initData() {
        PermissionsUtil.getInstance().requestPermission(activity, object : PermissionListener {
            override fun permissionGranted(permission: Array<String>) {
                loadStateView.setViewLoadState(LocalResStatesView.STATE_LOADING)
                LocalMediaHelper.getInstance().getLocalAlbumData(activity)
            }

            override fun permissionDenied(permission: Array<String>) {
                loadStateView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION)
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun observerIsCollecting() {
        shareViewModel?.isCollecting()?.observe(activity!!, object : Observer<Boolean> {
            override fun onChanged(collcting: Boolean) {
                sectionAdapter.showCheckbox(collcting)
                //取消编辑后重置数据的选中标记和选中数量
                if (!collcting) {
                    shareViewModel?.clearSelectedToCollectData()
                    sectionAdapter.data.map { it.mediaData?.isCheck = false }
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLocalAlumLoadEvent(event: LocalAlbumLoadEvent?) {
        //所有数据
        var headerCount = 0
        var currData: Date? = null
        val sectionMediaDataList = mutableListOf<SectionMediaData>()
        val mediaDataList = LocalMediaHelper.getInstance().mediaDataList
        for (item in mediaDataList) {
            //当前位置是照片日期变化位置，插入一个日期头部数据
            if (item.takeTime.toString() != currData.toString()) {
                headerCount++
                currData = item.takeTime
                sectionMediaDataList.add(SectionMediaData(true, currData))
            }
            sectionMediaDataList.add(SectionMediaData(false, null, headerCount, item))
        }
        sectionAdapter.setList(sectionMediaDataList)
        loadStateView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH)
    }


    private inner class PictureSectionAdapter : BaseSectionQuickAdapter<SectionMediaData, BaseViewHolder>(
            R.layout.item_picture_section_header,
            R.layout.item_picture) {

        private var showCheckbox = false

        fun showCheckbox(show: Boolean) {
            if (showCheckbox != show) {
                showCheckbox = show
                notifyDataSetChanged()
            }
        }

        override fun convertHeader(helper: BaseViewHolder, item: SectionMediaData) {
            Log.d("PictureSectionAdapter", "convertHeader: $item")
            helper.setText(R.id.tvDate, item.data.toString())
            helper.setText(R.id.tvWeek, DateUtils.getWeek(item.data))
        }

        @SuppressLint("SimpleDateFormat")
        override fun convert(holder: BaseViewHolder, item: SectionMediaData) {
            Log.d("PictureSectionAdapter", "convert: $item")
            val ivCover: ImageView = holder.getView(R.id.ivCover)
            val tvDuring: TextView = holder.getView(R.id.tvVideoDuring)
            val ivVideoFlag: ImageView = holder.getView(R.id.ivVideoFlag)
            val ivSelect: ImageView = holder.getView(R.id.cbSelect)
            val bottomMaskView: View = holder.getView(R.id.bottomMask)
            val totalMaskView: View = holder.getView(R.id.totalMask)
            val imageSize = DimensUtils.getDeviceWidth(context) / 4f

            item.mediaData?.let {
                when (item.mediaData) {
                    is ImageData -> {
                        GlideApp.with(this@PictureListFragment)
                                .asBitmap()
                                .load(item.mediaData.url)
                                .override(imageSize.toInt())
                                .into(ivCover)
                        tvDuring.visibility = View.GONE
                        ivVideoFlag.visibility = View.GONE
                        bottomMaskView.visibility = View.GONE
                    }
                    is VideoData -> {
                        GlideApp.with(this@PictureListFragment)
                                .load(item.mediaData.thumbnailPath)
                                .override(imageSize.toInt())
                                .into(ivCover)
                        tvDuring.apply {
                            visibility = View.VISIBLE
                            text = SimpleDateFormat("mm:ss").format(item.mediaData.duration)
                        }
                        ivVideoFlag.visibility = View.VISIBLE
                        bottomMaskView.visibility = View.VISIBLE
                    }
                }

                ivSelect.apply {
                    visibility = if (showCheckbox) View.VISIBLE else View.GONE
                    setBackgroundResource(if (showCheckbox && item.mediaData.isCheck) R.drawable.icon_picture_checked
                    else R.drawable.icon_picture_uncheck)
                }

                totalMaskView.visibility = if (showCheckbox && item.mediaData.isCheck) View.VISIBLE else View.GONE

                holder.itemView.setOnClickListener {
                    if (showCheckbox) {
                        item.mediaData.isCheck = !item.mediaData.isCheck
                        notifyItemChanged(getItemPosition(item))
                        if (item.mediaData.isCheck) {
                            shareViewModel?.addSelectedToCollectData(item.mediaData)
                        } else {
                            shareViewModel?.removeSelectedToCollectData(item.mediaData)
                        }
                    } else {
                        if (activity != null) {
                            PreviewActivityW7.start(activity, MAIN_ALBUM_NAME, getItemPosition(item) - item.headerCount, SHOW_ALL)
                        }
                    }
                }
            }
        }
    }

    data class SectionMediaData(
            override val isHeader: Boolean = false, //是否是分段头部
            val data: Date? = null,                 //分段头部时间
            val headerCount: Int = 0,               //当前item前面总共有几个分段头部
            val mediaData: MediaData? = null        //照片或视频数据
    ) : JSectionEntity()
}