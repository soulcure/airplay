package com.coocaa.tvpi.module.local.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coocaa.publib.data.local.MediaData

class PictureShareViewModel: ViewModel(){

    //是处于在加入收藏的编辑状态
    private var isCollectingLD: MutableLiveData<Boolean> = MutableLiveData()

    //选中的加入收藏的MediaData
    private var collectMediaDataLV:MutableLiveData<List<MediaData>> = MutableLiveData()
    private val collectMediaDataList= mutableListOf<MediaData>()

    fun isCollecting(): LiveData<Boolean> {
        return isCollectingLD
    }

    fun setCollecting(isCollecting: Boolean) {
        isCollectingLD.value = isCollecting
    }

    fun getCollectMediaData(): LiveData<List<MediaData>>{
        return collectMediaDataLV
    }

    fun addCollectMediaData(mediaData: MediaData){
        if(!collectMediaDataList.contains(mediaData)) {
            collectMediaDataList.add(mediaData)
            collectMediaDataLV.value = collectMediaDataList
        }
    }

    fun clearCollectMediaData(){
        collectMediaDataList.clear()
        collectMediaDataLV.value = collectMediaDataList
    }
}