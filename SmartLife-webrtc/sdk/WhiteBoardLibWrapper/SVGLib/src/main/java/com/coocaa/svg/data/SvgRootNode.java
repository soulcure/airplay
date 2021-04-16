package com.coocaa.svg.data;

import com.coocaa.define.SvgTagDef;

import java.util.List;

public class SvgRootNode extends SvgNode {
    public String version;
    public int width = 1920;
    public int height = 1080;

    public SvgRootNode() {
        tagName = SvgTagDef.ROOT;
    }

    List<SvgNode> childNodeList;
}
