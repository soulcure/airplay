package com.coocaa.smartscreen.data.store;
/**
 * 省份
 * Created by songxing on 2020/8/5
 */
public class ProvinceResp {
    private int  id_province;
    private String name;


    public int getIdProvince() {
        return id_province;
    }

    public void setIdProvince(int idProvince) {
        this.id_province = idProvince;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProvinceResp{" +
                "id_province=" + id_province +
                ", name='" + name + '\'' +
                '}';
    }
}
