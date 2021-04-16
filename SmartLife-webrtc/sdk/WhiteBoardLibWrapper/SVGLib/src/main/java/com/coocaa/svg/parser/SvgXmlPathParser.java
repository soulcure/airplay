/*
 * Copyright (C) 2015 Jorge Castillo Pérez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coocaa.svg.parser;

import android.graphics.Path;
import android.os.SystemClock;
import android.util.Log;

import com.coocaa.svg.data.SvgPathNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Entity to parse Svg paths to {@link Path} items understandable by the Android SDK. Obtained
 * from romainnurik Muzei implementation to avoid rewriting .
 *
 * @author romainnurik
 */
public class SvgXmlPathParser {

    private DocumentBuilder docBuilder;
    private Document mDoc;
    String TAG = "SVG-PathParser";

    public SvgXmlPathParser() {
        try {
          docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e)
        {
          e.printStackTrace();
        }
    }

    public SvgPathNode parse(String data) {
        Log.d(TAG, "start parse : " + data);
        long start = SystemClock.uptimeMillis();
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(data.getBytes("utf-8"));
            mDoc = docBuilder.parse(is);
        }  catch (Exception e) {
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
            return null;
        }
        Element element = mDoc.getDocumentElement();
        SvgPathNode node = new SvgPathNode();
        NamedNodeMap map = element.getAttributes();
        node.tagName = element.getTagName();

        if(map != null) {
            node.parse(map);
        }

        return node;
    }
}