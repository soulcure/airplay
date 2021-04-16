package com.coocaa.svg.data;

import android.text.TextUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SvgNode implements Serializable {
    public String id;
    public String owner;
    public String tagName;

    public SvgNode parentNode;
    public Map<String, String> attrMap;
    public static final String SPACE = " ";

    protected static final String TAG = "SVG-Node";

    public void setNodeValue(String value) {

    }

    public void parse(NamedNodeMap map) {
        int count = map.getLength();
        if(count > 0 && attrMap == null) {
            attrMap = new HashMap<>();
        }
        for(int i=0; i<count; i++) {
            Attr attr = (Attr)map.item(i);
//            Log.d(TAG, "attr name : " + attr.getName() + ", value=" + attr.getValue());
            attrMap.put(attr.getName(), attr.getValue());
            parse(attr.getName(), attr.getValue());
        }
    }

    protected boolean parse(String name, String value) {
        if("id".equals(name)) {
            this.id = value;
            return true;
        } else if("owner".equals(name)) {
            this.owner = value;
            return true;
        }
        return false;
    }

    public void fulfilAttrString(StringBuilder sb) {
        if(attrMap == null)
            return ;
        Iterator<Map.Entry<String, String>> iter = attrMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sb.append(SPACE).append(entry.getKey()).append("=\"").append(entry.getValue())
                    .append("\"").append(SPACE);
        }
    }

    public String toSvgString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<").append(tagName);
        fulfilAttrString(sb);
        fulfilSvgString(sb);
        if(!hasAttr("owner") && !TextUtils.isEmpty(owner)) {
            sb.append(SPACE).append("owner=\"").append(owner).append("\"").append(SPACE);
        }
        sb.append(" />");
        return sb.toString();
    }

    public void fulfilSvgString(StringBuilder sb) {

    }

    protected boolean hasAttr(String key) {
        return attrMap != null && attrMap.containsKey(key);
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
            return 0f;
        }
    }
}
