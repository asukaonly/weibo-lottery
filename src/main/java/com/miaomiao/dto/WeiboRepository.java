package com.miaomiao.dto;

import com.miaomiao.entity.Weibo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeiboRepository extends JpaRepository<Weibo, String> {
}
