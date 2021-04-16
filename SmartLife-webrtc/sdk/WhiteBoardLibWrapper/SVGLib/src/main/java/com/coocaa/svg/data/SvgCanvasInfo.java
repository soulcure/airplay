package com.coocaa.svg.data;

public class SvgCanvasInfo {
    public int x = 0;
    public int y = 0;
    public float scale = 1f;

    public void set(SvgCanvasInfo info) {
        if(info == null)
            return ;
        this.x = info.x;
        this.y = info.y;
        this.scale = info.scale;
    }

    public void setAttr(String key, String value) {
        if(C_X.equals(key)) {
            this.x = parseInt(value);
        } else if(C_Y.equals(key)) {
            this.y = parseInt(value);
        } else if(C_SCALE.equals(key)) {
            this.scale = parseFloat(value);
        }
    }

    private final static String C_X = "x";
    private final static String C_Y = "y";
    private final static String C_SCALE = "scale";


    public static boolean isValidKey(String key) {
        return C_X.equals(key) || C_Y.equals(key) || C_SCALE.equals(key);
    }

    protected int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    protected float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return 1f;
        }
    }

    public void toSvgString(StringBuilder sb) {
        sb.append(" x=\"").append(x).append("\" y=\"").append(y).append("\" scale=\"").append(scale).append("\" ");
    }

    public String toSvgString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" x=\"").append(x).append("\" y=\"").append(y).append("\" scale=\"").append(scale).append("\" ");
        return sb.toString();
    }
}
