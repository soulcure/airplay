/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2014-3-12         zhanghancheng
 *
 */

package com.coocaa.movie.data;

import java.io.Serializable;

/**
 * @ClassName ImageURL
 * @Description TODO (write something)
 * @author zhanghancheng
 * @date 2014-3-12
 * @version TODO (write something)
 */
public class ImageUrl implements Serializable
{
    /**
     * @Fields serialVersionUID TODO(write something)
     */
    private static final long serialVersionUID = 1L;

    /**
     * @Fields url TODO(图片地址)
     */
    public String url;
    
    /**
     * @Fields style TODO(横竖版，两个值：h（横）， v（竖）)
     */
    public String style;

    /**
     * @Fields size TODO(图片大小类型,b:大图，m:中图,s:小图)
     */
    public String size;

}
