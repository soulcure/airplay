package com.coocaa.tvpi.module.local.album2

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coocaa.publib.base.GlideApp
import com.coocaa.publib.data.local.ImageData
import com.coocaa.publib.data.local.VideoData
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.tvpi.base.BaseFragment
import com.coocaa.tvpi.event.LocalAlbumLoadEvent
import com.coocaa.tvpi.module.io.HomeUIThread
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper
import com.coocaa.tvpi.module.local.view.LocalResStatesView
import com.coocaa.tvpi.util.dp
import com.coocaa.tvpi.util.permission.PermissionListener
import com.coocaa.tvpi.util.permission.PermissionsUtil
import com.coocaa.tvpi.view.decoration.PictureItemDecoration
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.fragment_picture_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumListFragment : BaseFragment() {
    private lateinit var albumListAdapter: AlbumListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            EventBus.getDefault().register(this)
            initView()
            initData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    private fun initView() {
        albumListAdapter = AlbumListAdapter()
        recyclerview.apply {
            adapter = albumListAdapter
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(PictureItemDecoration(3, 10.dp, 20.dp))
        }
    }

    private fun initData() {
        PermissionsUtil.getInstance().requestPermission(activity, object : PermissionListener {
            override fun permissionGranted(permission: Array<String>) {
                LocalMediaHelper.getInstance().getLocalAlbumData(activity)
                loadStateView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH)
            }

            override fun permissionDenied(permission: Array<String>) {
                loadStateView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION)
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLocalAlumLoadEvent(event: LocalAlbumLoadEvent?) {
        val allMediaDataMap = LocalMediaHelper.getInstance().allMediaDataMap
        val albumList = mutableListOf<AlbumListBean>()
        for (item in allMediaDataMap) {
            if (!TextUtils.isEmpty(item.key) && item.value != null && item.value.isNotEmpty()) {
                val mediaData = item.value[0]
                if (mediaData is ImageData) {
                    albumList.add(AlbumListBean(item.key, mediaData.url, item.value.size))
                } else if (mediaData is VideoData) {
                    albumList.add(AlbumListBean(item.key, mediaData.thumbnailPath, item.value.size))
                }
            }
        }
        albumListAdapter.setList(albumList)
    }


    private inner class AlbumListAdapter : BaseQuickAdapter<AlbumListBean, BaseViewHolder>(R.layout.item_album) {
        override fun convert(holder: BaseViewHolder, item: AlbumListBean) {
            holder.setText(R.id.item_album_name, item.albumName)
            holder.setText(R.id.item_album_num, item.pictureCount.toString())
            val ivCover = holder.getView<ImageView>(R.id.item_album_cover)
            val imageSize = (DimensUtils.getDeviceWidth(context) - 50.dp) / 3f

            item.albumCover?.let {
                GlideApp.with(this@AlbumListFragment)
                        .load(item.albumCover)
                        .skipMemoryCache(true)
                        .override(imageSize.toInt())
                        .into(ivCover)
            }

            holder.itemView.setOnClickListener {
                if (activity != null) {
                    AlbumDetailActivity.starter(activity!!, item.albumName)
                }
            }
        }
    }

    data class AlbumListBean(
            val albumName: String,  //相册名字
            val albumCover: String?, //相册封面
            val pictureCount: Int   //相册中照片数量
    )
}