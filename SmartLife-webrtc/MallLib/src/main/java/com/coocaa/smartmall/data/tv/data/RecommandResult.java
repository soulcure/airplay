package com.coocaa.smartmall.data.tv.data;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

public class RecommandResult implements Serializable {
    /**
     * data_result : [{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718103832jyfl80.png","product_name":"智能落地扇","product_description":"智能落地扇","product_id":"3"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718103848xr7yfi.png","product_name":"扫地机器人","product_description":"扫地机器人","product_id":"4"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718103910gfarcm.png","product_name":"智能开门冰箱","product_description":"智能对门冰箱","product_id":"5"}},{"product":{"product_image":"\thttp://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718103937nzodgs.png","product_name":"智能滚筒洗衣机","product_description":"智能滚筒洗衣机","product_id":"6"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718105441xpw5p6.png","product_name":"创维电视W8","product_description":"创维电视W8","product_id":"7"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718105514mea4y1.png","product_name":"智能壁挂空调","product_description":"智能壁挂空调","product_id":"8"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718105538lswhvy.png","product_name":"立式空调","product_description":"立式空调","product_id":"9"}},{"product":{"product_image":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718105554rfxi5o.png","product_name":"触屏音箱","product_description":"触屏音箱","product_id":"10"}}]
     * resultcode : 200
     * reason : success
     */
    @JSONField(name = "resultcode")
    private String resultCode;
    private String reason;
    @JSONField(name = "data_result")
    private List<Recommand> dataResult;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultcode) {
        this.resultCode = resultcode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<Recommand> getDataResult() {
        return dataResult;
    }

    public void setDataResult(List<Recommand> data_result) {
        this.dataResult = data_result;
    }

    public static class Recommand implements Serializable {
        @JSONField(name = "product_id")
        private int productId; //表示的是产品的id
        @JSONField(name = "product_name")
        private String productName; //表示的是产品的名称
        @JSONField(name = "product_description")
        private String productDescription; //表示的产品的简单描述
        @JSONField(name = "product_image")
        private String productImage; //表示的是产品的图片Url

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getProductImage() {
            return this.productImage;
        }

        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }
    }
}
