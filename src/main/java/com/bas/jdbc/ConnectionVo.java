package com.bas.jdbc;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionVo {

    private String name;
    
//    private Connection con;
    private DataSource dataSource;
    
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    
    public SqlQuery sq;
    
    public ConnectionVo(Map<String, String> param) {
        driverClassName = param.get("driverClassName");
        this.setUrl(param.get("url"));
        this.setUsername(param.get("username"));
        this.setPassword(param.get("password"));
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
//                DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//                dataSourceBuilder.driverClassName(driverClassName);
//                dataSourceBuilder.url(url);
//                dataSourceBuilder.username(username);
//                dataSourceBuilder.password(password);
//                dataSource = dataSourceBuilder.build();
//                ds.
//                ((HikariDataSource) dataSource).close();
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setDriverClassName(driverClassName);
                hikariConfig.setJdbcUrl(url);
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setMaximumPoolSize(5);
                hikariConfig.setConnectionTimeout(10000);
                hikariConfig.setIdleTimeout(600000);
                hikariConfig.setMaxLifetime(1800000);
                dataSource = new HikariDataSource(hikariConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }
    
    public Connection getConnection() throws Exception {
//        if(con == null || con.isClosed()) {
//            con = getDataSource().getConnection();
//        }
        return ((HikariDataSource)getDataSource()).getConnection();
    }

    public void closeDataSource() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
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
    
    public SqlQuery getSqlQuery() {
        if(sq == null) {
            if(this.driverClassName.indexOf("sqlserver") > -1) {
                sq = new SqlQueryMssqlImpl(this);
            }else if(this.driverClassName.indexOf("mariadb") > -1) {
                sq = new SqlQueryMariadbImpl(this);
            }
        }
        return sq;
    }
    public String getXmlFileName() {
        if(this.driverClassName.indexOf("sqlserver") > -1) {
            return "MS-SQL.xml";
        }else if(this.driverClassName.indexOf("mariadb") > -1) {
            return "MARIADB.xml";
        }
        return "";
    }
}
