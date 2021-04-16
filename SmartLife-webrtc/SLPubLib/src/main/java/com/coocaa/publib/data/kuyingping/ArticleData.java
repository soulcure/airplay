package com.coocaa.publib.data.kuyingping;

import java.io.Serializable;

/**
 * @ClassName ArticleData
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/12/12
 * @Version TODO (write something)
 */
public class ArticleData implements Serializable {
    public int id; //id
    public String classifyCode; //分类code banner
    public String classifyName; //分类名称
    public int periodsNum; //期数num
    public String periodsName; //期数
    public int articleId; //文章id
    public String articleTitle; //文章标题
    public String articleAuthor; //文章作者
    public String articlePicUrl; //图片地址
    public String articleReadTime; //阅读时长
    public int articleType; //类型，0影评，1话题
    public String createTime;
}
