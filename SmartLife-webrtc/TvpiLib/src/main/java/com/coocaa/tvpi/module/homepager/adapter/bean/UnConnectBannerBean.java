package com.coocaa.tvpi.module.homepager.adapter.bean;


import java.util.ArrayList;
import java.util.List;

public class UnConnectBannerBean {
    public Integer imageRes;
    public String imageUrl;
    public String title;
    public int viewType;

    public UnConnectBannerBean(Integer imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public UnConnectBannerBean(String imageUrl, String title, int viewType) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.viewType = viewType;
    }


    public static List<UnConnectBannerBean> getTestData() {
        List<UnConnectBannerBean> list = new ArrayList<>();
        list.add(new UnConnectBannerBean("https://img.zcool.cn/community/013de756fb63036ac7257948747896.jpg", null, 1));
        list.add(new UnConnectBannerBean("https://img.zcool.cn/community/01639a56fb62ff6ac725794891960d.jpg", null, 1));
        list.add(new UnConnectBannerBean("https://img.zcool.cn/community/01270156fb62fd6ac72579485aa893.jpg", null, 1));
        list.add(new UnConnectBannerBean("https://img.zcool.cn/community/01233056fb62fe32f875a9447400e1.jpg", null, 1));
        list.add(new UnConnectBannerBean("https://img.zcool.cn/community/016a2256fb63006ac7257948f83349.jpg", null, 1));
        return list;
    }
}
