package com.coocaa.whiteboard.ui.callback;

/**
 * 工具层UI点击回调
 */
public interface ToolLayerCallback {
    /**
     * 工具退出
     * @param isClearCanvas 是否清空画板
     */
    void onExitClick(boolean isClearCanvas);

    /**
     * 橡皮 清空画板
     */
    void onEraserClearAllClick();

    /**
     * 工具更多 保存图片
     */
    void onMoreSavePicClick();


    /**
     * 电视显示区域设置
     * @param width
     * @param height
     * @param scale
     * @param posX
     * @param posY
     */
    void onTvCanvasChange(int width,int height,float scale,int posX,int posY);
}
