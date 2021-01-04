package com.lei2j.sms.bomb.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author leijinjun
 * @date 2020/12/21
 **/
public class BaseDao {

    protected final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate parameterJdbcTemplate;

    public BaseDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate parameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.parameterJdbcTemplate = parameterJdbcTemplate;
    }
}
