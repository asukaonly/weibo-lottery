package com.miaomiao.entity;

import com.miaomiao.dto.JPAEntity;
import lombok.*;
import lombok.extern.log4j.Log4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lyl on 2017-8-8.
 */

@Entity
@Table(name = "weibo",
        indexes = {@Index(name = "id", columnList = "id", unique = true),
                @Index(name = "uid", columnList = "uid"),
                @Index(name = "mid", columnList = "mid")})
@Data
@Log4j
public class Weibo implements Serializable {
    //ID
    @Id
    private String id;

    //用户id
    @Column(nullable = false, length = 50)
    private String uid;

    //微博id
    @Column(nullable = false, length = 50)
    private String mid;

    //微博内容
    @Column(columnDefinition="TEXT")
    private String content;

    //是否完成关注
    private boolean isFollowed;

    //是否需要转发
    private boolean needForwarded;
    //是否完成转发
    private boolean isForwarded;

    //是否需要评论
    private boolean needReplayed;
    //是否完成评论
    private boolean isReplayed;

    //是否完成任务
    private boolean isCompleted;

    //话题
    private String topic;

    //更新时间
    private Date updateDate;
}
