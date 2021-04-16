package com.coocaa.smartmall.data.mobile.data;

import java.util.List;

public class BannerResult extends BaseResult {
    @Override
    public String toString() {
        return "BannerResult{" +
                "data=" + data +
                '}';
    }

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        @Override
        public String toString() {
            return "DataBean{" +
                    "product_id='" + product_id + '\'' +
                    ", image_url='" + image_url + '\'' +
                    '}';
        }

        /**
         * product_id : 15
         * image_url : http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200720181630iygnq0.png
         */

        private String product_id;
        private String image_url;

        public String getProduct_id() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }
    }
}
