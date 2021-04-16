package com.coocaa.movie.web.product;

import java.io.Serializable;

public class SourceButton implements Serializable {

    public String title;//按钮上显示的文字；文字过长时，客户端做跑马类效果；

    public String action;//该按钮的点击事件响应命令

}
