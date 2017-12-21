package com.miaomiao.entity;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "weibo",
        indexes = {@Index(name = "mid", columnList = "mid", unique = true),
                @Index(name = "uid", columnList = "uid")})
@Data
@Log4j
public class Weibo implements Serializable {
    //微博id
    @Id
    @Column(nullable = false, length = 50)
    private String mid;

    //用户id
    @Column(nullable = false, length = 50)
    private String uid;

    //微博内容
    @Column(columnDefinition = "TEXT")
    private String content;

    //是否完成关注
    private boolean isFollowed;

    //是否需要转发
    private boolean needForward;
    //是否完成转发
    private boolean isForwarded;

    //是否需要评论
    private boolean needReply;
    //是否完成评论
    private boolean isReplyed;

    //是否完成任务
    private boolean isCompleted;

    //话题
    private String topic;

    //更新时间
    private Date updateDate;
}
