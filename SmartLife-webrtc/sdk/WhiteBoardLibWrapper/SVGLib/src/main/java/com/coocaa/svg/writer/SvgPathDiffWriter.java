package com.coocaa.svg.writer;

import com.coocaa.svg.render.SvgPaint;

public class SvgPathDiffWriter {

    private final static String prefix = "<path ";
    private final static String suffix = " />";

    public static String toXml(String pathString) {
        return toXml(pathString, null);
    }

    public static String toXml(String pathString, SvgPaint svgPaint) {
        if(pathString.startsWith(prefix))
            return pathString;
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(pathString);
        if(svgPaint != null) {
            sb.append(svgPaint.toXmlString());
        }
        sb.append(suffix);

        return sb.toString();
    }
}
