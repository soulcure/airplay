package com.coocaa.movie.product.m.entity;

import com.coocaa.movie.data.ImageUrl;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class ProductItem implements Serializable {

    /**
     * 产品ID（券ID），该产品在后台的唯一标识
     */
    private int product_id;

    public int singleVipPrice;

    public int singlePrice;

    public boolean is_focus;

    public boolean isComBinePkg;

    public int getIs_group() {
        return is_group;
    }

    public void setIs_group(int is_group) {
        this.is_group = is_group;
    }

    /**
     * 	组合标志:1标示此产品包作为单点组合产品包,0标示此产品包非单点组合产品包
     */
    private int is_group;

    /**
     * 产品名称（券名称）
     */
    private String product_name;

    /**
     * 产品类型，每家合作商的标识可能不一样(代表包年、包月等)；
     * 如果是券产品，则是券类型；
     */
    private String product_type;

    /**
     * 正常单价,单位：分
     */
    private int unit_fee;

    /**
     * 折扣后单价，单位：分
     */
    private int discount_fee;

    /**
     * 当前产品是否支持与其他优惠活动叠加；true支持，false不支持（默认）；为客户端提供优惠券使用逻辑判断。
     */
    private boolean support_other_discount;

    /**
     * 该产品是否为推荐购买产品；true是，false否（默认）；用于将产品源绑定频道时，获取推荐购买产品的价格信息用于展示。
     */
    private boolean is_recommend;

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public int getUnit_fee() {
        return unit_fee;
    }

    public void setUnit_fee(int unit_fee) {
        this.unit_fee = unit_fee;
    }

    public int getDiscount_fee() {
        return discount_fee;
    }

    public int getVip_fee() {
        return vip_fee;
    }

    public void setVip_fee(int vip_fee) {
        this.vip_fee = vip_fee;
    }

    private int vip_fee;

    public void setDiscount_fee(int discount_fee) {
        this.discount_fee = discount_fee;
    }

    public boolean isSupport_other_discount() {
        return support_other_discount;
    }

    public void setSupport_other_discount(boolean support_other_discount) {
        this.support_other_discount = support_other_discount;
    }

    public boolean isIs_recommend() {
        return is_recommend;
    }

    public void setIs_recommend(boolean is_recommend) {
        this.is_recommend = is_recommend;
    }

    public int getBuy_count() {
        return buy_count;
    }

    public void setBuy_count(int buy_count) {
        this.buy_count = buy_count;
    }

    public int getLeave_count() {
        return leave_count;
    }

    public void setLeave_count(int leave_count) {
        this.leave_count = leave_count;
    }

    public int getShow_style() {
        return show_style;
    }

    public void setShow_style(int show_style) {
        this.show_style = show_style;
    }

    public boolean isIs_alert() {
        return is_alert;
    }

    public void setIs_alert(boolean is_alert) {
        this.is_alert = is_alert;
    }

    public int getProduct_level() {
        return product_level;
    }

    public void setProduct_level(int product_level) {
        this.product_level = product_level;
    }

    public String getExtend_attribute() {
        return extend_attribute;
    }

    public void setExtend_attribute(String extend_attribute) {
        this.extend_attribute = extend_attribute;
    }

    public List<ImageUrl> getImages() {
        return images;
    }

    public void setImages(List<ImageUrl> images) {
        this.images = images;
    }

    public ValidTimes getValid_times() {
        return valid_times;
    }

    public void setValid_times(ValidTimes valid_times) {
        this.valid_times = valid_times;
    }

    public String getBase_desc() {
        return base_desc;
    }

    public void setBase_desc(String base_desc) {
        this.base_desc = base_desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getSource_id() {
        return source_id;
    }

    public void setSource_id(int source_id) {
        this.source_id = source_id;
    }

    private int source_id;

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon_json() {
        return icon_json;
    }

    public void setIcon_json(String icon_json) {
        this.icon_json = icon_json;
    }

    public boolean isIs_support_direct_discount() {
        return is_support_direct_discount;
    }

    public void setIs_support_direct_discount(boolean is_support_direct_discount) {
        this.is_support_direct_discount = is_support_direct_discount;
    }

    public int getProduct_stock() {
        return product_stock;
    }

    public void setProduct_stock(int product_stock) {
        this.product_stock = product_stock;
    }

    public List<DiscountProducts> getDiscount_products() {
        return discount_products;
    }

    public void setDiscount_products(List<DiscountProducts> discount_products) {
        this.discount_products = discount_products;
    }

    public AllowanceInfo getAllowance_info() {
        return allowance_info;
    }

    public void setAllowance_info(AllowanceInfo allowance_info) {
        this.allowance_info = allowance_info;
    }

    public int getIs_support_advanced_demand() {
        return is_support_advanced_demand;
    }

    public void setIs_support_advanced_demand(int is_support_advanced_demand) {
        this.is_support_advanced_demand = is_support_advanced_demand;
    }

    /**
     * 每次下订单购买数量，默认为1
     */
    private int buy_count;

    /**
     * 券产品剩余数量，默认为0
     */
    private int leave_count;

    /**
     客户端展示样式；
     0普通包月的样式，显示价格信息（默认）；
     1单点样式，显示价格，但不显示原价；
     2券样式，不显示价格，显示pay_unit、leave_count和produce_name等。
     */
    private int show_style;

    /**
     下订单前提示用户是否购买；true提示，false不提示(默认)；
     如果是券产品，一般设置为true。
     */
    private boolean is_alert;

    /**
     * 产品分类级别；
     0-表示未上线的产品，
     1-表示用于销售的普通产品包，
     2-表示必须付费单点包，
     3-表示在线但对用户不可见用于兑换或赠送的产品,
     4-表示单点用券产品,
     5-表示在线但根据策略输出的产品,
     6-表示会员单点包,
     7-表示自动续费产品,
     8-表示特别配置的产品包，配合extend_attribut字段使用，例如：黄金VIP、领取电影票入口
     */
    private int product_level;

    /**
     * 扩展属性
     GOLDVIP表示黄金VIP；
     TIPS表示提示信息类产品包；
     */
    private String extend_attribute;

    /**
     * 该产品的配图
     */
    private List<ImageUrl> images;

    /**
     * 有效时间单位
     */
    private ValidTimes valid_times;


    public static class ValidTimes implements Serializable {
        /**
         * 该产品有效期时长

         */
        public int count;

        /**
         * 该产品有效期时长单位；"y"表示年，"se"表示季,"m"表示月，"d"表示天，"h"表示小时,"mi"表示分钟。默认为d。
         */
        public String unit;
    }

    /**
     * 产品基础描述信息；比如显示“30元/月”等字样。
     */
    private String base_desc;

    /**
     * 产品详细描述信息
     */
    private String desc;

    /**
     * 角标
     */
    private String icon_json;

    /**
     * 是否支持直接在当前产品算折扣，true支持，false不支持;
     支持时直接在当前产品算出折扣，并把当前的产品product_id提交下单接口的product_id和discount_product_id；
     不支持时需要从discount_products节点匹配折扣后的产品信息，并把当前的产品product_id提交到下单接口的product_id，discount_products列表中的discount_product_id提交到下单接口的discount_product_id
     */
    private boolean is_support_direct_discount;

    /**
     * 产品包库存；
     -1表示不限库存；
     is_support_discount_product为true时，有折扣并且限制了库存，则在此字段上校验
     */
    private int product_stock;

    private List<DiscountProducts> discount_products;

    public static class DiscountProducts implements Serializable {
        /**
         * 折扣产品id
         */
        public int discount_product_id;

        /**
         * 折扣产品单价，单位：分19000
         */
        public int discount_product_fee;

        /**
         * 折扣信息，下单时需传上来
         */
        public String discount_info;

        /**
         * 优惠信息图标。
         * {
         * "allowance_icon":"津贴角标",
         * "coupon_icon":"优惠券角标"
         * }
         */
        public String discount_images;

        public boolean other_discount;

        /**
         * 折扣产品包库存；
         -1表示不限库存；
         is_support_discount_product为false时，有折扣并且限制了库存，则在此字段上校验
         */
        public int discount_product_stock;
    }

    /**
     * 津贴信息
     */
    private AllowanceInfo allowance_info;

    private static class AllowanceInfo implements Serializable {
        /**
         * 津贴活动ID,使用津贴时需要传此值上来
         */
        public int allowance_act_id;

        /**
         * 津贴活动code(与津贴系统相对应)
         */
        public String subsidy_code;

        /**
         * 津贴活动开始时间戳，例如：1475251200000（单位：毫秒）
         */
        public long activity_start_time;

        /**
         * 津贴活动结束时间戳，例如：1475251200000（单位：毫秒）
         */
        public long activity_end_time;

        /**
         * 津贴开始可以使用时间戳，例如：1475251200000（单位：毫秒）
         */
        public long start_use_time;

        /**
         * 津贴结束可以使用时间戳，例如：1475251200000（单位：毫秒）
         */
        public long end_use_time;

        /**
         * 津贴规则集合
         */
        public List<AllowanceSchemes> allowance_schemes;
    }

    private static class AllowanceSchemes implements Serializable {
        /**
         * 限制津贴使用阀值，单位:分(原产品包金额高于此阀值充许使用津贴)
         */
        public int allowance_check_money;

        /**
         * 实际使用津贴金额，单位:分
         */
        public int allowance_discount_fee;

        /**
         * 津贴余额阀值，单位:分(余额高于此阀值充充许使用津贴)
         */
        public int subsidy_blance;

        /**
         * 津贴角标图
         */
        public String allowance_scheme_icon;
    }

    /**
     * 是否超前点播产品包,0：否，1：是
     */
    private int is_support_advanced_demand;

    /**
     * 超前点播相关信息，当is_support_advanced_demand为1时此字段有效
     */
    public List<AdvancedDemandInfo> advanced_demand_info;

    public String advanced_demand_info_msg;

    public static class AdvancedDemandInfo implements Serializable {
        /**
         * 	购买模式；
         1全集:  2:单集  4:更新集
         */
        public int option;

        /**
         * 剧集购买顺序 0-不限制，1-按顺序购买，2 大结局锁定
         */
        public int pay_order;

        public String discount_info;

        /**
         * 是否可买
         0:可买
         1:价格配置错误
         2:用户非vip
         3:用户已购买
         4:前置剧集未购买
         5:星光可播
         6:其他原因
         7:购买cid vid时只剩一个vid
         8:购买vid数量超上限（仅更新集）
         */
        public int buy_check;

        /**
         * 超前点播有效期
         */
        public String valid_days;

        /**
         * TV标题特殊化文本（第一行）
         */
        public String tv_main_title_text;

        /**
         * 专辑/视频 名称后缀文本
         */
        public String tv_box_cid_name_desc;

        /**
         * 购买集数文本， 全N集等
         */
        public String tv_box_episode_text;

        /**
         * 价格后文本
         */
        public String tv_box_price_desc;

        /**
         * TV锁定单集购买标题
         */
        public String tv_limit_title;

        /**
         * TV锁定单集购买副标题
         */
        public String tv_limit_sub_title;

        /**
         * TV锁定单集购买跳转全集按钮文案
         */
        public String tv_limit_buy_all_button;

        public int discount_fee;
        public int unit_fee;

        /**
         * 产品包是否显示  0 显示  1 不显示
         */
        public int show_status;
    }

    public static String resetPrice(int price)
    {
        DecimalFormat df =new DecimalFormat("0.00");
        String priceText = df.format(((float)price / (float)100));
        if(priceText.endsWith(".00"))
        {
            return String.valueOf(price / 100);
        }
        return priceText;
    }
}
