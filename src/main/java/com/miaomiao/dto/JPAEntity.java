package com.miaomiao.dto;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by lyl on 2017-8-10.
 */
public abstract class JPAEntity<T> extends QueryDslJpaRepository<T, String> {
    public JPAEntity(JpaEntityInformation<T, String> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
}
