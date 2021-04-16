package com.coocaa.tvpi.module.local.document;

import com.coocaa.tvpilib.R;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/11/25
 */
public enum FormatEnum {
    //文档格式
    WORD("Word", R.drawable.icon_doc_word, "docx", "doc"),

    //电子表格
    EXCEL("Excel", R.drawable.icon_doc_xls, "xls", "xlsx"),

    //ppt
    PPT("PPT", R.drawable.icon_doc_ppt, "ppt", "pptx"),

    //pdf
    PDF("PDF", R.drawable.icon_doc_pdf, "pdf"),

    //未知格式
    UNKNOWN("Unknown", R.drawable.icon_doc_ppt);

    public String type;
    public int icon;
    public String[] formats;

    /**
     * @param type    文件类型
     * @param icon    对应icon
     * @param formats 包含格式
     */
    FormatEnum(String type, int icon, String... formats) {
        this.type = type;
        this.icon = icon;
        this.formats = formats;
    }

    /**
     * 通过文件类型获取对应枚举
     *
     * @param extension 文件扩展名
     * @return 文件对应的枚举信息，如果没有，返回未知
     */
    public static FormatEnum getFormat(String extension) {
        for (FormatEnum format : FormatEnum.values()) {
            for (String extend : format.formats) {
                if (extend.equalsIgnoreCase(extension)) {
                    return format;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * 是否包含对应格式
     *
     * @param type
     * @return
     */
    public static boolean contains(String type) {
        for (FormatEnum format : FormatEnum.values()) {
            if (format.type.equalsIgnoreCase(FormatEnum.UNKNOWN.type)) {
                continue;
            }
            if (format.type.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
