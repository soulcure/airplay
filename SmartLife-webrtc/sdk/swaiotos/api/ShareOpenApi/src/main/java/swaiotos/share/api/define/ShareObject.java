package swaiotos.share.api.define;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class ShareObject implements Serializable {
    public String type; //分享内容类型，参考ShareType定义
    public String text; //分享内容
    public String title; //标题
    public String description; //描述内容
    public String thumb; //缩略图
    public String url; //打开地址
    public String from;
    public String version;
    public Map<String, String> extra; //拓展参数

    public transient ShareType sType;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ShareObject{");
        sb.append("type='").append(type).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", thumb='").append(thumb).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", from='").append(from).append('\'');
        sb.append(", extra=").append(extra);
        sb.append('}');
        return sb.toString();
    }
}
