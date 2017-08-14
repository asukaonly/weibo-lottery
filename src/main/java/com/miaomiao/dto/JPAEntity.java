package com.miaomiao.dto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by lyl on 2017-8-10.
 */
public interface JPAEntity<T> extends JpaRepository<T, String> {
    
}
