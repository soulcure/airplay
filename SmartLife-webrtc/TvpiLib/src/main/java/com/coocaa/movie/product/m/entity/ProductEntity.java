package com.coocaa.movie.product.m.entity;

import java.io.Serializable;
import java.util.List;

public class ProductEntity implements Serializable {

    public String productSource;//超级影视VIP、 奇异果VIP

    public String giftTip;//赠送提示  开通送好礼

    public List<ProductItem> products;

    public String company;

    /**
     * 产品包模板配置内容(Json)
     */
    private String show_template;

    /**
     * 该产品列表搭配的背景图，即对应产品源的背景图
     */
    private String background_image;

    /**
     * dmpCode
     */
    public String dmp_code;

    /**
     * 产品包策略id
     */
    public String policy_id;

    /**
     * 产品包id
     */
    public int scheme_id;

    /**
     * 产品包id
     */
    private int source_id;

    private String source_sign;

    /**
     * 运营颜色，图片资源
     */
    private String source_json;

    /**
     * 产品源标识
     */
    private String source;

    private String source_name;
}
