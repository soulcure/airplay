package com.coocaa.publib.data.kuyingping;

import java.io.Serializable;

public class ArticleDetailData implements Serializable {
    public String digest;
    public int id;
    public int type;
    public String content;
    public int status;
    public String createTime;
    public String picUrl;
    public String articleTitle;
    public String author;
    public String readTime;
    public int collectNum;
    public String createName;
    public String isCollectByWxopenid;

    public Object articleContactList; //相关文章内容
    public Object voteList; //文章关联投票
    public Object readyVoteList; //该文章中的该用户已经投过票的投票id集合
    public Object articleMoviesList; // 文章关联影片
    public Object voteOptionWrapList; // 投票的选项
    public Object commentList;


}
