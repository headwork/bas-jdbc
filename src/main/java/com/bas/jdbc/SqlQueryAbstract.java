package com.bas.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public abstract class SqlQueryAbstract implements SqlQuery {
    protected ConnectionVo cv;
    protected String xmlFileName;
    protected Map<String, String> MAP_SQL = new HashMap<String, String>();
    
    public SqlQueryAbstract(ConnectionVo cv) {
        this.cv = cv;
    }
    
    public List<Map<String, Object>> selectExec(String strSql) {
        Connection con = null;
        Statement ctmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            con = cv.getConnection();
            ctmt = con.createStatement();
            rs = ctmt.executeQuery(strSql);
            list = convertListMap(rs);
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {if(rs != null) rs.close();} catch (Exception e) {}
            try {if(ctmt != null) ctmt.close();} catch (Exception e) {}
            try {if(con != null) con.close();} catch (Exception e) {}
        }

        return list;
    }
    
    public Map<String, Object> selectExecInfo(String strSql) {
        Connection con = null;
        PreparedStatement ctmt = null;
        ResultSet rs = null;
        Map<String, Object> result = new HashMap<String,Object>();
        List<Map<String, Object>> header = null;
        List<Map<String, Object>> list = null;
        
        try {
            con = cv.getConnection();
            ctmt = con.prepareStatement(strSql);
            rs = ctmt.executeQuery();
            if(rs == null){
                return result;
            }
            list = convertListMap(rs);
            header = columnInfo(rs);

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<String> tableList = extractTableNames(strSql);
            List<String> columnList = new ArrayList<String>();
            for (int i = 1; i <= columns; ++i) {
                columnList.add(md.getColumnName(i));
            }
            List<Map<String, Object>> header2 = objectInfo(con, tableList, columnList);
            int size = header2.size();
            for (var i = 0; i<columns; i++) {
                Map item = header.get(i);
                for (int j = 0; j < size; j++) {
                    String colName = (String) header2.get(j).get("COLUMN_NAME");
                    if(colName.equalsIgnoreCase((String)item.get("name"))) {
                        item.putAll(header2.get(j));
                    }
                }
            }
            result.put("list", list);
            result.put("columnInfo", header);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {if(ctmt != null) ctmt.close();} catch (Exception e) {}
            try {if(con != null) con.close();} catch (Exception e) {e.printStackTrace();}
        }

        return result;
    }

    @Override
    public Map<String, Object> tableInfo(Map<String, Object> params) {
        params.put("schema", Util.convertQueryParam((String)params.get("database")));
        params.put("table", Util.convertQueryParam((String)params.get("table")));
        params.put("table_name", Util.convertQueryParam((String)params.get("table1")));
        Map<String, Object> result = new HashMap<String,Object>();
        result.put("list", selectList("table_info", params));
        result.put("constraint_info", selectList("constraint_info", params));
        return result;
    }

    @Override
    public Map<String, Object> tableList(Map<String, Object> params) {
        params.put("schema", Util.convertQueryParam((String)params.get("database")));
        params.put("table", Util.convertQueryParam((String)params.get("table")));
        Map<String, Object> result = new HashMap<String,Object>();
        result.put("list", selectList("table_list", params));
        return result;
    }
    
    @Override
    public Map<String, Object> databaseList() {
        Map<String, Object> result = new HashMap<String,Object>();
        result.put("list", selectList("schema_list", new HashMap<String,Object>()));
        return result;
    }

    public List<Map<String, Object>> selectList(String sqlId, Map<String, Object> params) {
        Connection con = null;
        Statement ctmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            con = cv.getConnection();
            String template = templateQeury(sqlId);
            String sql = freeMarkerQeury(template, params);
            System.out.println("sql = " + sql);
            ctmt = con.createStatement();
            rs = ctmt.executeQuery(sql);
            list = convertListMap(rs, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {if(ctmt != null) ctmt.close();} catch (Exception e) {}
            try {if(con != null) con.close();} catch (Exception e) {e.printStackTrace();}
        }

        return list;
    }
    
    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList) throws Exception {
        return objectInfo(con, tableList, columnList, false); 
    }
    
    @Override
    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList,
            boolean isPk) throws Exception {
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
        String template = templateQeury("column_info");
        Map<String, Object> params = new HashMap<>();
        params.put("tables", tables);
        params.put("cols", cols);
        params.put("pkTag", "N");
        String sql = freeMarkerQeury(template, params);
//        System.out.println("sb.toString() = " + sql);
        Statement ctmt = null;
        ResultSet rs = null;
        try {
            ctmt = con.createStatement();
            rs = ctmt.executeQuery(sql);
            List<Map<String, Object>> list = convertListMap2(rs, 200);
            return list;
        } catch (Exception e) {
            throw e;
        }finally {
            try {if(ctmt != null) ctmt.close();} catch (Exception e) {} 
        }
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
            Map<String,Object> row = new LinkedHashMap<String, Object>();
            for(int i=1; i<=columns; ++i) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
            if(rows >= max) break;
            
        }
        return list;
    }
    
    /**
     * CaseInsensitiveKeyMap 사용 (key값 대소문자 무시)
     * @param rs
     * @param max
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> convertListMap2(ResultSet rs, int max) throws Exception{
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        if(rs == null) return list;

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
     
        int rows = 0;
        while(rs.next()) {
            Map<String,Object> row = new CaseInsensitiveKeyMap<Object>();
            for(int i=1; i<=columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
            if(rows >= max) break;
            
        }
        return list;
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
    
    public String templateQeury(String templateId) {
        if(!MAP_SQL.containsKey(templateId)) {
            if(xmlFileName == null) {
                xmlFileName = cv.getXmlFileName();
            }
            String sql = loadTemplate(xmlFileName, templateId);
//            System.out.println("sql = " + sql);
            MAP_SQL.put(templateId, sql);
        }
        return MAP_SQL.get(templateId);
    }

    
    public static String freeMarkerQeury(String template, Map<String, Object> params) {
        try {
            // FreeMarker 구성 설정
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);

            // 템플릿 생성
            Template t = new Template("templateName", template, cfg);

            // 데이터 모델 생성
            Map<String, Object> root = params;

            // 템플릿 처리
            StringWriter out = new StringWriter();
            t.process(root, out);

            return out.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }
    
    public static String loadTemplate(String xmlFileName, String templateId) {
        try {
            File xmlFile = getXmlFile(xmlFileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("template");
            String template = null;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Element element = (Element) nList.item(temp);
                if (element.getAttribute("id").equals(templateId)) {
                    template = element.getTextContent();
                    break;
                }
            }
            return template;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static File getXmlFile(String fileName) {
        String currentDir = System.getProperty("user.dir");
        File xmlFile = new File(currentDir + "/sql/" + fileName);
        if(xmlFile.exists()) return xmlFile;
        
        xmlFile = new File(currentDir + "/dist/sql/" + fileName);
        if(xmlFile.exists()) return xmlFile;
        
        xmlFile = new File(currentDir + "/lib/" + fileName);
        if(xmlFile.exists()) return xmlFile;
        return null;
    }
}
