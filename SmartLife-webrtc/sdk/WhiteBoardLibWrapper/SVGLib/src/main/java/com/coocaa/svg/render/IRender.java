package com.coocaa.svg.render;

import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.data.SvgNode;

/**
 * @Author: yuzhan
 */
public interface IRender {

    void setPaint(SvgPaint paint);

    void callRender();

    void setCanvasInfo(SvgCanvasInfo canvasInfo);

    void renderXml(String renderXml) throws RenderException;

    /**
     * 单条渲染
     * @param renderData
     */
    SvgNode renderDiff(String renderData) throws RenderException;

    SvgNode renderDiff(String renderData,boolean render) throws RenderException;

    SvgNode renderDiffXml(String diffXmlRenderData);

    void clear();

    SvgData getSvgData();
}
