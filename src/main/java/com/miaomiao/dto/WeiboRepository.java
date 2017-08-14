package com.miaomiao.dto;

import com.miaomiao.entity.Weibo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by lyl on 2017-8-14.
 */
public interface WeiboRepository extends JpaRepository<Weibo,String>{
}
