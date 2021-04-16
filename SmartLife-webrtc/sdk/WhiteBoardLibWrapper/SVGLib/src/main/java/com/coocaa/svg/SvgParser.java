package com.coocaa.svg;

import android.os.SystemClock;
import android.util.Log;

import com.coocaa.define.SvgTagDef;
import com.coocaa.interfaces.IDataParser;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.data.SvgGroupNode;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.data.SvgPathNode;
import com.coocaa.svg.data.SvgRootNode;
import com.coocaa.svg.data.SvgTextNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SvgParser implements IDataParser<SvgData> {

    private DocumentBuilder docBuilder;
    private Document mDoc;

    String TAG = "SVG-Parser";

    public SvgParser() {
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public SvgData parse(String data) {
        Log.d(TAG, "start parse : " + data);
        SvgData svgData = new SvgData();
        long start = SystemClock.uptimeMillis();
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(data.getBytes("utf-8"));
            mDoc = docBuilder.parse(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(mDoc == null) {
            return svgData;
        }
        Element rootElement = mDoc.getDocumentElement();
        parseElement(svgData, rootElement);
        return svgData;
    }

    private boolean parseElement(SvgNode node, Element element) {
        NamedNodeMap map = element.getAttributes();
        node.tagName = element.getTagName();
        if(map != null) {
            node.parse(map);
        }
        int childCount = element.getChildNodes().getLength();
        Log.d(TAG, "parseElement, tag=" + element.getTagName() + ", childCount=" + childCount);
        if(childCount > 0) {//有子节点
            for(int i=0; i<childCount; i++) {
                Node childNode = element.getChildNodes().item(i) ;
                Log.d(TAG, "child.nodeType=" + childNode.getNodeType() + ", child.nodeName=" + childNode.getNodeName() + ", child.nodeValue=" + childNode.getNodeValue());
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    SvgNode childSvgNode = createNode(((Element) childNode).getTagName());
                    if(parseElement(childSvgNode, (Element)childNode)) {
                        if(node instanceof SvgGroupNode) {
                            Log.d(TAG, "parent node " + node + ", add child : " + childSvgNode);
                            ((SvgGroupNode) node).addNode(childSvgNode);
                        }
                        childSvgNode.parentNode = node;
                    }
                } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                    node.setNodeValue(childNode.getNodeValue());
                }
            }
        }
        return true;
    }

    private SvgNode createNode(String tagName) {
        SvgNode node = null;
        if(SvgTagDef.ROOT.equals(tagName)) {
            node = new SvgRootNode();
        } else if(SvgTagDef.GROUP.equals(tagName)) {
            node = new SvgGroupNode();
        } else if(SvgTagDef.PATH.equals(tagName)) {
            node = new SvgPathNode();
        } else if(SvgTagDef.TEXT.equals(tagName)) {
            node = new SvgTextNode();
        } else {
            node = new SvgNode();
        }
        node.tagName = tagName;
        Log.d(TAG, "create node, tag=" + tagName + ", node=" + node);
        return node;
    }
}
