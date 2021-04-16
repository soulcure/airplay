package com.coocaa.define;

public class SvgTagDef {

    public final static String ROOT = "svg";
    public final static String GROUP = "g"; //group
    public final static String PATH = "path"; //path

    public final static String TEXT = "text";//文本
    public final static String CIRCLE = "circle"; //圆

    public final static String LINE = "line"; //直线

    public final static String RECT = "rect"; //矩形

    public final static String OVAL = "ellipse"; //椭圆

    public final static String POLY_LINE = "polyline"; //折线、曲线
    public final static String POLY_GON = "polygon"; //多边形


    /**
     * path示例
     * <path d="M 100 350 q 150 -300 300 0" stroke="blue"
     *   stroke-width="5" fill="none" />
     */


    /**
     * 圆形示例
     * <circle cx="100" cy="50" r="40" stroke="black"
     *   stroke-width="2" fill="red"/>
     */

    /**
     * 矩形示例
     * <rect x="20" y="20" width="250" height="250"
     *     style="fill:blue;stroke:pink;stroke-width:5;
     *     fill-opacity:0.1;stroke-opacity:0.9;opacity:0.9"/>
     */

    /**
     * 文本示例
     *  <text x="0" y="15" fill="red">I love SVG</text>
     */

    /**
     * 折线、曲线示例
     * <polyline points="0,0 0,20 20,20 20,40 40,40 40,60"
     * style="fill:white;stroke:red;stroke-width:2"/>
     */

    /**
     * 多边形示例
     * <polygon points="220,100 300,210 170,250"
     * style="fill:#cccccc;
     * stroke:#000000;stroke-width:1"/>
     */

}
