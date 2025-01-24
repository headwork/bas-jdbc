package com.bas.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConnectionTool {
    Map<String, ConnectionVo> mapCon = new HashMap<String, ConnectionVo>();
    
    /* 섹션으로 관리 */
    public boolean containsKey(String name) {
        return mapCon.containsKey(name);
    }
    
    public boolean isConnect(String name) {
        if(mapCon.containsKey(name)) {
            
        }
        return false;
    }
    
    public void setConnection(String name, ConnectionVo cv) {
        mapCon.put(name, cv);
    }
    
    public void setConnection(String name, Map<String, String> param) {
        ConnectionVo cv = new ConnectionVo();
        cv.setDriverClassName(param.get("driverClassName"));
        cv.setUrl(param.get("url"));
        cv.setUsername(param.get("username"));
        cv.setPassword(param.get("password"));
        mapCon.put(name, cv);
    }
    
    public void select() {
        
    }
    
    public List<Map<String, Object>> selectExec(String dbName, String strSql) {
        ConnectionVo cv = mapCon.get(dbName);
        Connection con = null;
        List<Map<String, Object>> list = null;
        try {
            con = cv.getConnection();
            Statement ctmt = con.createStatement();
            ResultSet rs = ctmt.executeQuery(strSql);
            list = convertListMap(rs);
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                if(con != null) con.close();
            } catch (Exception e) {}
        }

        return list;
    }
    public Map<String, Object> selectExecInfo(String dbName, String strSql) {
        ConnectionVo cv = mapCon.get(dbName);
        Connection con = null;
        Map<String, Object> result = new HashMap<String,Object>();
        List<Map<String, Object>> header = null;
        List<Map<String, Object>> list = null;
        try {
            con = cv.getConnection();
            Statement ctmt = con.createStatement();
            ResultSet rs = ctmt.executeQuery(strSql);
            if(rs == null){
                return result;
            }
            list = convertListMap(rs);
            header = columnInfo(rs);
            
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<String> tableList = extractTableNames(strSql);
            List<String> columnList = new ArrayList<String>();
            StringBuilder sb2 = new StringBuilder();
            for (int i = 1; i <= columns; ++i) {
                columnList.add(md.getColumnName(i));
//                sb2.append("OR (TABLE_NAME = '").append(md.getTableName(i))
//                    .append("' AND COLUMN_NAME = '").append(md.getColumnName(i)).append("')");
            }

            List<Map<String, Object>> header2 = objectInfo(con, tableList, columnList);
            int size = header2.size();
            for (var i = 0; i<columns; i++) {
                Map item = header.get(i);
                for (int j = 0; j < size; j++) {
                    if(item.get("name").equals(header2.get(j).get("COLUMN_NAME"))) {
                        item.putAll(header2.get(j));
                    }
                }
            }
            result.put("list", list);
            result.put("columnInfo", header);
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                if(con != null) con.close();
            } catch (Exception e) {}
        }

        return result;
    }
    
    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList) throws Exception {
        return objectInfo(con, tableList, columnList, false); 
    }
    
    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList, boolean isPk) throws Exception {
        StringBuilder sb = new StringBuilder();
        String cols = columnList.stream()
                .map(s -> "'" + s + "'") // 각 문자열을 작은따옴표로 감쌈
                .collect(Collectors.joining(", ")); // 쉼표와 공백으로 연결
        if(columnList.size() == 0) {
            cols = "''";
        }
        
        String tables = tableList.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        if(tableList.size() == 0) {
            tables = "''";
        }
        
        sb.append("WITH WTB_LIST AS (")
            .append( "\n    SELECT *")
            .append( "\n      FROM (")
            .append( "\n    SELECT T.NAME AS TABLE_NAME, T.id")
            .append( "\n         , COUNT(1) OVER( PARTITION BY COL.NAME) AS COL_CNT")
            .append( "\n         , ROW_NUMBER() OVER( PARTITION BY COL.NAME ORDER BY ISNULL(CMT.VALUE, '') DESC, ISNULL(TCMT.VALUE, '') DESC, T.NAME) AS RANK01")
            .append( "\n         , ISNULL(TCMT.VALUE, '') AS TABLE_COMMENT")
            .append( "\n         , COL.NAME AS COLUMN_NAME")
            .append( "\n         , CMT.VALUE AS COLUM_COMMENT")
            .append( "\n         , A.DATA_TYPE")
            .append( "\n         , CASE WHEN A.CHARACTER_MAXIMUM_LENGTH = -1 THEN 'MAX'")
            .append( "\n                ELSE ISNULL(CAST(A.CHARACTER_MAXIMUM_LENGTH AS VARCHAR), CAST(A.NUMERIC_PRECISION AS VARCHAR)) END AS LEN")
            .append( "\n         , A.NUMERIC_SCALE SCALE")
            .append( "\n         , CASE WHEN A.IS_NULLABLE = 'YES' THEN '' ELSE 'N' END IS_NULLABLE")
            .append( "\n         , CASE WHEN A.COLUMN_DEFAULT IS NOT NULL THEN SUBSTRING(A.COLUMN_DEFAULT, 2, LEN(A.COLUMN_DEFAULT) -2) END AS COLUMN_DEFAULT")
            .append( "\n      FROM sysobjects T")
            .append( "\n     INNER JOIN sys.columns COL")
            .append( "\n        ON T.id = COL.object_id")
            .append( "\n      LEFT JOIN INFORMATION_SCHEMA.COLUMNS A")
            .append( "\n        ON A.TABLE_NAME = T.name")
            .append( "\n       AND A.COLUMN_NAME = COL.name")
            .append( "\n      LEFT JOIN sys.extended_properties TCMT  ")
            .append( "\n        ON TCMT.major_id = T.id AND TCMT.minor_id = 0")
            .append( "\n       AND TCMT.name = 'MS_Description'")
            .append( "\n      LEFT JOIN  sys.extended_properties CMT  ")
            .append( "\n        ON CMT.major_id = COL.object_id AND CMT.minor_id = COL.column_id")
            .append( "\n     WHERE T.xtype = 'U'")
            .append( "\n       AND T.NAME IN (").append(tables).append(")")
            .append( "\n       AND COL.NAME IN (").append(cols).append(")")
            .append( "\n       ) T")
            .append( "\n       WHERE RANK01 = 1")
            .append( "\n)")
            .append( "\n, WTB_LIST2 AS (")
            .append( "\n    SELECT *")
            .append( "\n      FROM (")
            .append( "\n    SELECT T.NAME AS TABLE_NAME, T.id")
            .append( "\n         , COUNT(1) OVER( PARTITION BY COL.NAME) AS COL_CNT")
            .append( "\n         , ROW_NUMBER() OVER( PARTITION BY COL.NAME ORDER BY ISNULL(CMT.VALUE, '') DESC, ISNULL(TCMT.VALUE, '') DESC, T.NAME) AS RANK01")
            .append( "\n         , ISNULL(TCMT.VALUE, '') AS TABLE_COMMENT")
            .append( "\n         , COL.NAME AS COLUMN_NAME")
            .append( "\n         , CMT.VALUE AS COLUM_COMMENT")
            .append( "\n         , A.DATA_TYPE")
            .append( "\n         , CASE WHEN A.CHARACTER_MAXIMUM_LENGTH = -1 THEN 'MAX'")
            .append( "\n                ELSE ISNULL(CAST(A.CHARACTER_MAXIMUM_LENGTH AS VARCHAR), CAST(A.NUMERIC_PRECISION AS VARCHAR)) END AS LEN")
            .append( "\n         , A.NUMERIC_SCALE SCALE")
            .append( "\n         , CASE WHEN A.IS_NULLABLE = 'YES' THEN '' ELSE 'N' END IS_NULLABLE")
            .append( "\n         , CASE WHEN A.COLUMN_DEFAULT IS NOT NULL THEN SUBSTRING(A.COLUMN_DEFAULT, 2, LEN(A.COLUMN_DEFAULT) -2) END AS COLUMN_DEFAULT")
            .append( "\n      FROM sysobjects T")
            .append( "\n     INNER JOIN sys.columns COL")
            .append( "\n        ON T.id = COL.object_id")
            .append( "\n      LEFT JOIN INFORMATION_SCHEMA.COLUMNS A")
            .append( "\n        ON A.TABLE_NAME = T.name")
            .append( "\n       AND A.COLUMN_NAME = COL.name")
            .append( "\n      LEFT JOIN sys.extended_properties TCMT  ")
            .append( "\n        ON TCMT.major_id = T.id AND TCMT.minor_id = 0")
            .append( "\n       AND TCMT.name = 'MS_Description'")
            .append( "\n      LEFT JOIN  sys.extended_properties CMT  ")
            .append( "\n        ON CMT.major_id = COL.object_id AND CMT.minor_id = COL.column_id ")
            .append( "\n     WHERE T.xtype = 'U'")
            .append( "\n       AND COL.NAME IN (").append(cols).append(")")
            .append( "\n       AND COL.NAME NOT IN(SELECT COLUM_COMMENT FROM WTB_LIST)")
            .append( "\n       ) T")
            .append( "\n     WHERE RANK01 = 1")
            .append( "\n)")
            .append( "\n    SELECT * FROM WTB_LIST")
            .append( "\n    UNION ALL")
            .append( "\n    SELECT * FROM WTB_LIST2")
        ;
        if (isPk) {
            sb.append( "\n    SELECT * FROM WTB_LIST")
            .append( "\n    UNION ALL")
            .append( "\n    SELECT * FROM WTB_LIST2")
            ;
        } else {
            sb.append("\nSELECT T.*").append("\n     , CASE WHEN P.COLUMN_NAME IS NULL THEN '' ELSE 'Y' END PK_YN")
                    .append("\n  FROM (")
                    .append( "\n    SELECT * FROM WTB_LIST")
                    .append( "\n    UNION ALL")
                    .append( "\n    SELECT * FROM WTB_LIST2")
                    .append("\n       ) T")
                    .append("\n  LEFT JOIN sys.indexes ix ON IX.object_id = T.id AND IX.is_primary_key = 1")
                    .append("\n  LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS P")
                    .append("\n    ON IX.NAME = P.CONSTRAINT_NAME")
                    .append("\n   AND P.TABLE_NAME = T.TABLE_NAME AND P.COLUMN_NAME = T.COLUMN_NAME");
        }
            
//        System.out.println("sb.toString() = " + sb.toString());
        Statement ctmt = con.createStatement();
        ResultSet rs = ctmt.executeQuery(sb.toString());
        return convertListMap(rs);
    }
    
    public List<Map<String, Object>> columnInfo(ResultSet rs) throws Exception {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List list = new ArrayList<Map<String, Object>>();
        String[] cols = new String[columns];
        for(int i=1; i<=columns; ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", md.getColumnName(i));
            map.put("precision", md.getPrecision(i));
            map.put("scale", md.getScale(i));
            map.put("type", md.getColumnTypeName(i));
            map.put("label", md.getColumnLabel(i));
            list.add(map);
        }
        return list;
    }
    
    public static List<Map<String, Object>> convertListMap(ResultSet rs) throws Exception{
        return convertListMap(rs, 30);
    }
    public static List<Map<String, Object>> convertListMap(ResultSet rs, int max) throws Exception{
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        if(rs == null) return list;

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
     
        int rows = 0;
        while(rs.next()) {
            Map<String,Object> row = new LinkedHashMap<String, Object>(columns);
            for(int i=1; i<=columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
            if(rows >= max) break;
            
        }
        return list;
    }
    
    private List<String> extractTableNames(String sql) {
        List<String> tableNames = new ArrayList<>();
        // 간단한 FROM 절 추출 (더 복잡한 쿼리에 대해서는 수정 필요)
        Pattern pattern = Pattern.compile("\\bFROM\\b\\s+([\\w.]+)");
        Matcher matcher = pattern.matcher(sql.toUpperCase()); // 대소문자 구분 없이 검색

        while (matcher.find()) {
            tableNames.add(matcher.group(1).trim());
        }

        // JOIN 절 추출 (간단한 INNER JOIN만 고려)
         pattern = Pattern.compile("\\bJOIN\\b\\s+([\\w.]+)");
         matcher = pattern.matcher(sql.toUpperCase());
         while (matcher.find()) {
             tableNames.add(matcher.group(1).trim());
         }

        return tableNames;
    }
}