package com.example.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class MyDao {

    private DataSource dataSource;

    @Autowired
    public MyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
