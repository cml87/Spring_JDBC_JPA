package com.example.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MyDao_DaoSupport extends JdbcDaoSupport {

    @Autowired
    public MyDao_DaoSupport(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @PostConstruct
    public void doQuery(){
        
        String result = getJdbcTemplate().queryForObject("select 1 from dual", String.class);
        System.out.println("result = " + result);

    }


}
