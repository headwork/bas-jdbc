package com.bas.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import com.google.gson.Gson;

public class ConnectionVo {

    private String name;
    
    private Connection con;
    private DataSource dataSource;
    
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    
    public ConnectionVo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private DataSource getDataSource() {
        try {
            if(dataSource == null) {
                DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
                dataSourceBuilder.driverClassName(driverClassName);
                dataSourceBuilder.url(url);
                dataSourceBuilder.username(username);
                dataSourceBuilder.password(password);
                dataSource = dataSourceBuilder.build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }
    
    public Connection getConnection() throws Exception {
        if(con == null || con.isClosed()) {
            con = getDataSource().getConnection();
        }
        return con;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
