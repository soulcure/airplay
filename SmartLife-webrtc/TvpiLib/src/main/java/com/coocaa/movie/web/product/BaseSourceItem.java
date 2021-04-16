package com.coocaa.movie.web.product;


import com.coocaa.movie.data.ImageUrl;

import java.io.Serializable;
import java.util.List;

public class BaseSourceItem implements Serializable {

    /**
     * 产品源ID,产品源的唯一标识
     */
    private int source_id;

    /**
     产品源标志；
     "yinhe"表示爱奇艺在线影视会员包；
     "yinhe-km"表示爱奇艺卡密；
     "6"表示腾讯企鹅影院会员包；
     "7"表示腾讯鼎级剧场会员包；
     "tx-km"表示这是腾讯卡密；
     */
    private String source_sign;

    /**
     * 产品源名称
     */
    private String source_name;

    /**
     产品源类型
     0支付型产品源(默认)；
     1卡密兑换产品源；
     2活动信息产品源；
     */
    private int source_type;

    /**
     * 概述：根据 style 和 size 获取显示的图片地址<br/>
     */
    public String getImage(String stylefilter, String sizeFilter)
    {
        try
        {
            if (images != null && images.size() > 0)
            {
                if (stylefilter == null || stylefilter.equals(""))
                {
                    stylefilter = "";
                }
                if (sizeFilter == null || sizeFilter.equals(""))
                {
                    sizeFilter = "";
                }
                for (ImageUrl i : images)
                {
                    if (i != null && i.style != null && i.size != null)
                    {
                        if ( stylefilter.contains(i.style) && sizeFilter.contains(i.size))
                        {
                            return i.url;
                        }
                    }
                }
                for (ImageUrl i : images)
                {
                    if (i != null && i.style != null)
                    {
                        if (stylefilter.contains(i.style))
                        {
                            return i.url;
                        }
                    }
                }
                return images.get(0).url;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     商户ID；
     1026爱奇艺；
     1028腾讯；
     */
    private String appcode;

    /**
     支持购买此产品源的账号类型列表；
     0普通酷开账号；
     1绑定了QQ的酷开账号；
     */
    private List<String> account_type;

    /**
     * 该产品源的配图
     */
    private List<ImageUrl> images;

    /**
     获取有效期途径；
     0不获取有效期（默认）；
     1从当前返回结果valid_scope获取；
     2自行调用第三方SDK获取
     */
    private String valid_way;

    /**
     * 有效期类型；0无有效期（这时valid_scope为空）；1会员包有效期；默认为1。
     */
    private int valid_type;

    /**
     * 有效时间
     */
    private ValidScope valid_scope;

    /**
     * 当前用户是否已经对该产品源（会员服务）设置自动续费；0未设置（默认）；1已设置；
     */
    private int keeppay;

    /**
     自动续费功能当前使用状态；仅当keeppay=1时有效；
     0表示续费功能正常使用中（默认），大于0时均表示异常；
     1表示续费功能由于扣费失败处于异常状态；
     2表示续费功能由于扣费失败次数过多，已处于冻结状态；
     */
    private int keeppay_status;

    /**
     * 小海报的点击事件响应命令，action
     */
    private String cmd_action;

    /**
     * 0表示基础产品源,1表示聚合产品源(超级vip)
     */
    private int source_model;

    /**
     * TV端源图标信息,即vip源图标
     */
    private String source_icon;

    /**
     * 产品源描述
     */
    private String description;

    /**
     * 推送类型，0未设置（谁都不推送），1推送给客户端，2满足策略时推送给客户端，3根据活动赠送（不推送给客户端），10卡密；
     */
    private int push_type;

    public int getSource_id() {
        return source_id;
    }

    public void setSource_id(int source_id) {
        this.source_id = source_id;
    }

    public String getSource_sign() {
        return source_sign;
    }

    public void setSource_sign(String source_sign) {
        this.source_sign = source_sign;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public int getSource_type() {
        return source_type;
    }

    public void setSource_type(int source_type) {
        this.source_type = source_type;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }

    public List<String> getAccount_type() {
        return account_type;
    }

    public void setAccount_type(List<String> account_type) {
        this.account_type = account_type;
    }

    public List<ImageUrl> getImages() {
        return images;
    }

    public void setImages(List<ImageUrl> images) {
        this.images = images;
    }

    public String getValid_way() {
        return valid_way;
    }

    public void setValid_way(String valid_way) {
        this.valid_way = valid_way;
    }

    public int getValid_type() {
        return valid_type;
    }

    public void setValid_type(int valid_type) {
        this.valid_type = valid_type;
    }

    public ValidScope getValid_scope() {
        return valid_scope;
    }

    public void setValid_scope(ValidScope valid_scope) {
        this.valid_scope = valid_scope;
    }

    public int getKeeppay() {
        return keeppay;
    }

    public void setKeeppay(int keeppay) {
        this.keeppay = keeppay;
    }

    public int getKeeppay_status() {
        return keeppay_status;
    }

    public void setKeeppay_status(int keeppay_status) {
        this.keeppay_status = keeppay_status;
    }

    public String getCmd_action() {
        return cmd_action;
    }

    public void setCmd_action(String cmd_action) {
        this.cmd_action = cmd_action;
    }

    public int getSource_model() {
        return source_model;
    }

    public void setSource_model(int source_model) {
        this.source_model = source_model;
    }

    public String getSource_icon() {
        return source_icon;
    }

    public void setSource_icon(String source_icon) {
        this.source_icon = source_icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPush_type() {
        return push_type;
    }

    public void setPush_type(int push_type) {
        this.push_type = push_type;
    }
}
