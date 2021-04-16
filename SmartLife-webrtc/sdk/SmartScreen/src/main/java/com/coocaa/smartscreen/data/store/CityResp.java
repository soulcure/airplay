package com.coocaa.smartscreen.data.store;

/**
 * 城市
 * Created by songxing on 2020/8/5
 */
public class CityResp {
    private int id_city;
    private int id_province;
    private String name;

    public int getIdCity() {
        return id_city;
    }

    public void setIdCity(int idCity) {
        this.id_city = idCity;
    }

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
        return "CityResp{" +
                "id_city=" + id_city +
                ", id_province=" + id_province +
                ", name='" + name + '\'' +
                '}';
    }
}
