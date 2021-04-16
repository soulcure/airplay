package com.coocaa.svg.data;

import android.text.TextUtils;

import java.util.Iterator;

public class SvgData extends SvgGroupNode {
    public boolean isXmlSvg = true; //是否是完整的svg xml 数据结构

    private SvgNode lastNode; //最后一个节点

    private final static String xmlPrefix = "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
            "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
            "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
            "xmlns=\"http://www.w3.org/2000/svg\">\n";

    private final static String xmlSuffix = "\n</svg>";

    public SvgData() {

    }

    public int childSize() {
        return childNodeList == null ? 0 : childNodeList.size();
    }

    @Override
    public String toSvgString() {
        if(isXmlSvg) {
            StringBuilder sb = new StringBuilder();
            sb.append(xmlPrefix);
            if(childNodeList != null) {
                for(SvgNode svgNode : childNodeList) {
                    sb.append(svgNode.toSvgString());
                }
            }
            sb.append(xmlSuffix);
            return sb.toString();
        } else {
            return xmlPrefix + xmlSuffix;
        }
    }

    public SvgNode getLastNode() {
        return lastNode;
    }

    @Override
    public void addNode(SvgNode node) {
        super.addNode(node);
        this.lastNode = node;
    }

    //优先让最后一个子Group来add
    public void addNodeWithGroup(SvgNode node) {
        if(lastNode instanceof SvgGroupNode) {//首次不走这里
            ((SvgGroupNode) lastNode).addNode(node);
        } else {
            super.addNode(node);
            this.lastNode = node;
        }
    }

    public void remove() {

    }

    public void removeNode(String id) {
//        if(nodeList != null) {
//            Iterator<SvgNode> iter = nodeList.iterator();
//            while(iter.hasNext()) {
//                SvgNode node = iter.next();
//                if(TextUtils.equals(node.id, id)) {
//                    iter.remove();
//                    break;
//                }
//            }
//        }
    }

    public void reRender() {

    }
}
