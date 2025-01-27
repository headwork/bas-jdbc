package com.bas.jdbc;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface SqlQuery {
    public List<Map<String, Object>> selectExec(String strSql);
    public Map<String, Object> selectExecInfo(String strSql);
    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList, boolean isPk) throws Exception;
}
