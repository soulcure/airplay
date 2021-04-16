package com.coocaa.tvpi.module.local.album2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coocaa.publib.data.local.MediaData

class PictureShareViewModel: ViewModel(){

    //是处于在加入收藏的编辑状态
    private var isCollectingLD: MutableLiveData<Boolean> = MutableLiveData()

    //选中的加入收藏的MediaData
    private var selectedToCollectDataLV:MutableLiveData<List<MediaData>> = MutableLiveData()
    private val selectedToCollectDataList= mutableListOf<MediaData>()

    //全选或者取消全选（收藏时用到）
    private var isSelectAllLD: MutableLiveData<Boolean> = MutableLiveData()

    //是否刷新收藏数据列表（加入、取消收藏时用到）
    private var isNotifyCollectChangeLD: MutableLiveData<Boolean> = MutableLiveData()

    fun isCollecting(): LiveData<Boolean> {
        return isCollectingLD
    }

    fun setCollecting(isCollecting: Boolean) {
        isCollectingLD.value = isCollecting
    }

    fun isNotifyCollectChange(): LiveData<Boolean> {
        return isNotifyCollectChangeLD
    }

    fun setNotifyCollectChange(isNotifyChange: Boolean) {
        isNotifyCollectChangeLD.value = isNotifyChange
    }

    fun isSelectAllData(): LiveData<Boolean> {
        return isSelectAllLD
    }

    fun setSelectAllData(selectAll: Boolean) {
        isSelectAllLD.value = selectAll
    }

    fun getSelectedToCollectData(): LiveData<List<MediaData>>{
        return selectedToCollectDataLV
    }

    fun addSelectedToCollectData(mediaData: MediaData){
        if(!selectedToCollectDataList.contains(mediaData)) {
            selectedToCollectDataList.add(mediaData)
            selectedToCollectDataLV.value = selectedToCollectDataList
        }
    }

    fun addSelectedToCollectData(mediaDataList: List<MediaData>){
        for (mediaData in mediaDataList) {
            if(!selectedToCollectDataList.contains(mediaData)) {
                selectedToCollectDataList.add(mediaData)
            }
        }
        selectedToCollectDataLV.value = selectedToCollectDataList
    }

    fun removeSelectedToCollectData(mediaData: MediaData){
        if(selectedToCollectDataList.contains(mediaData)) {
            selectedToCollectDataList.remove(mediaData)
            selectedToCollectDataLV.value = selectedToCollectDataList
        }
    }

    fun clearSelectedToCollectData(){
        selectedToCollectDataList.clear()
        selectedToCollectDataLV.value = selectedToCollectDataList
    }
}