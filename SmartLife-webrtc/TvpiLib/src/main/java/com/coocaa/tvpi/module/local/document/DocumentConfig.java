package com.coocaa.tvpi.module.local.document;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 文档相关的参数配置
 * @Author: wzh
 * @CreateDate: 12/3/20
 */
public class DocumentConfig {

    /**
     * 需要扫描的文档路径定义
     */
    private final static String PATH_DINGTALK = "DingTalk";//钉钉
    private final static String PATH_WEIXINWORK = "tencent/WeixinWork/filecache";//企业微信
    private final static String PATH_WEIXIN_OLD = "tencent/MicroMsg/Download";//旧版微信
    private final static String PATH_WEIXIN = "Android/data/com.tencent.mm/MicroMsg/Download";//新版微信
    private final static String PATH_QQ = "Android/data/com.tencent.mobileqq/Tencent/QQfile_recv";//QQ
    /**
     * 需要扫描的文档路径数组
     */
    public final static String[] ALL_SCAN_PATHS = {"下载", "Download", "Documents", PATH_DINGTALK, PATH_QQ, PATH_WEIXIN, PATH_WEIXIN_OLD, PATH_WEIXINWORK};
    /**
     * 下拉菜单-文档来源
     */
    public final static List<String> SPINNER_SOURCES = new ArrayList<>();
    /**
     * 下拉菜单-文档格式
     */
    public final static List<String> SPINNER_FORMATS = new ArrayList<>();

    /**
     * 支持的文档类型
     */
    public final static List<FormatEnum> SUPPORT_FORMATS = new ArrayList<>();

    public final static String FORMAT_ALL_TEXT = "全部格式";

    static {
        SUPPORT_FORMATS.add(FormatEnum.PPT);
        SUPPORT_FORMATS.add(FormatEnum.PDF);
        SUPPORT_FORMATS.add(FormatEnum.WORD);
        SUPPORT_FORMATS.add(FormatEnum.EXCEL);

        SPINNER_FORMATS.add(FORMAT_ALL_TEXT);
        for (FormatEnum format : SUPPORT_FORMATS) {
            SPINNER_FORMATS.add(format.type);
        }

        for (Source source : Source.values()) {
            SPINNER_SOURCES.add(source.text);
        }
    }

    public enum Source {
        ALL("全部来源"),
        WEIXINWORK("企业微信", PATH_WEIXINWORK),
        QQ("QQ", PATH_QQ),
        WEIXIN("微信", PATH_WEIXIN, PATH_WEIXIN_OLD),
        DINGTALK("钉钉", PATH_DINGTALK);

        public String text;
        public String[] scanPaths;//第三方的才赋值，其他归类All

        /**
         * @param text      显示的文本
         * @param scanPaths 扫描路径
         */
        Source(String text, String... scanPaths) {
            this.text = text;
            this.scanPaths = scanPaths;
        }
    }

    public static Source getSourceByText(String text) {
        for (Source source : Source.values()) {
            if (source.text.equalsIgnoreCase(text)) {
                return source;
            }
        }
        return Source.ALL;
    }

    public static Source getSourceByPath(String path) {
        for (Source source : Source.values()) {
            for (String p : source.scanPaths) {
                if (p.equalsIgnoreCase(path)) {
                    return source;
                }
            }
        }
        return Source.ALL;
    }

}
