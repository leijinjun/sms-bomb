package com.lei2j.sms.bomb.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author leijinjun
 * @date 2021/1/4
 **/
@NoRepositoryBean
public interface CommonJpaRepository<T,ID> extends JpaRepositoryImplementation<T,ID> {

}
