package swaiotos.runtime.base.style;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class AppletTitleStyle implements Serializable {
    Float alpha = null;
    Integer color = null;
    Boolean fakeBold = null;
    Integer textSize = null;

    public AppletTitleStyle setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public Float getAlpha() {
        return alpha;
    }

    public AppletTitleStyle setColor(int color) {
        this.color = color;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public AppletTitleStyle setFakeBold(boolean fakeBold) {
        this.fakeBold = fakeBold;
        return this;
    }

    public Boolean isFakeBold() {
        return fakeBold;
    }

    public void setTextSize(Integer textSize) {
        this.textSize = textSize;
    }

    public Integer getTextSize() {
        return textSize;
    }
}
