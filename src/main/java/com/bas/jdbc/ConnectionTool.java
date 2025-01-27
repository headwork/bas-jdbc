package com.bas.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionTool {
    Map<String, ConnectionVo> mapCon = new HashMap<String, ConnectionVo>();
    
    /* 섹션으로 관리 */
    public boolean containsKey(String name) {
        return mapCon.containsKey(name);
    }
    
    public boolean isConnect(String name) {
        return mapCon.containsKey(name);
    }
    
    public void setConnection(String name, ConnectionVo cv) {
        mapCon.put(name, cv);
    }
    
    public void removeConnection(String name) {
        if(containsKey(name)) {
            ConnectionVo cv = mapCon.get(name);
            cv.closeDataSource();
        }
    }
    
    public void setConnection(String name, Map<String, String> param) {
        ConnectionVo cv = new ConnectionVo(param);
        removeConnection(name);
//        cv.setDriverClassName(param.get("driverClassName"));
//        cv.setUrl(param.get("url"));
//        cv.setUsername(param.get("username"));
//        cv.setPassword(param.get("password"));
        mapCon.put(name, cv);
    }

    public SqlQuery getSqlQuery(String name) {
        ConnectionVo cv = mapCon.get(name);
        return cv.getSqlQuery();
    }
    
//    public List<Map<String, Object>> selectExec(String dbName, String strSql) {
//        ConnectionVo cv = mapCon.get(dbName);
////        Connection con = null;
////        List<Map<String, Object>> list = null;
////        try {
////            con = cv.getConnection();
////            Statement ctmt = con.createStatement();
////            ResultSet rs = ctmt.executeQuery(strSql);
////            list = convertListMap(rs);
////        } catch (Exception e) {
//////            e.printStackTrace();
////            throw new RuntimeException(e);
////        }finally {
////            try {
////                if(con != null) con.close();
////            } catch (Exception e) {}
////        }
//        cv.sel
//        return list;
//    }
//    public Map<String, Object> selectExecInfo(String dbName, String strSql) {
//        ConnectionVo cv = mapCon.get(dbName);
//        Connection con = null;
//        Map<String, Object> result = new HashMap<String,Object>();
//        List<Map<String, Object>> header = null;
//        List<Map<String, Object>> list = null;
//        try {
//            con = cv.getConnection();
//            Statement ctmt = con.createStatement();
//            ResultSet rs = ctmt.executeQuery(strSql);
//            if(rs == null){
//                return result;
//            }
//            list = convertListMap(rs);
//            header = columnInfo(rs);
//            
//            ResultSetMetaData md = rs.getMetaData();
//            int columns = md.getColumnCount();
//            List<String> tableList = extractTableNames(strSql);
//            List<String> columnList = new ArrayList<String>();
//            StringBuilder sb2 = new StringBuilder();
//            for (int i = 1; i <= columns; ++i) {
//                columnList.add(md.getColumnName(i));
////                sb2.append("OR (TABLE_NAME = '").append(md.getTableName(i))
////                    .append("' AND COLUMN_NAME = '").append(md.getColumnName(i)).append("')");
//            }
//
//            List<Map<String, Object>> header2 = objectInfo(con, tableList, columnList);
//            int size = header2.size();
//            for (var i = 0; i<columns; i++) {
//                Map item = header.get(i);
//                for (int j = 0; j < size; j++) {
//                    if(item.get("name").equals(header2.get(j).get("COLUMN_NAME"))) {
//                        item.putAll(header2.get(j));
//                    }
//                }
//            }
//            result.put("list", list);
//            result.put("columnInfo", header);
//        } catch (Exception e) {
////            e.printStackTrace();
//            throw new RuntimeException(e);
//        }finally {
//            try {
//                if(con != null) con.close();
//            } catch (Exception e) {}
//        }
//
//        return result;
//    }

}