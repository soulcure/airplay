package com.coocaa.movie.web.product;

import java.io.Serializable;
import java.util.List;

public class PaySourceModel implements Serializable {

    /**
     * 展示在TV右上角的提示信息；文字过长时，客户端做跑马类效果；在Header中cAppVersion>=2190000之后的客户端版本才使用此字段。
     */
    private String tips;

    /**
     * 展示在TV右上角的按钮列表；为空值或个数为0时，表示没有按钮。在Header中cAppVersion>=2190000之后的客户端版本才使用此字段，且目前默认只取第一个用于显示；
     */
    private List<SourceButton> buttons;

    /**
     * 产品源列表
     */
    private List<BaseSourceItem> sources;

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public List<SourceButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<SourceButton> buttons) {
        this.buttons = buttons;
    }

    public List<BaseSourceItem> getSources() {
        return sources;
    }

    public void setSources(List<BaseSourceItem> sources) {
        this.sources = sources;
    }

}
