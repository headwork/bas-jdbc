package com.bas.jdbc;

public class SqlQueryMssqlImpl extends SqlQueryAbstract {
    public SqlQueryMssqlImpl(ConnectionVo cv) {
        super(cv);
    }

//    @Override
//    public List<Map<String, Object>> objectInfo(Connection con, List<String> tableList, List<String> columnList,
//            boolean isPk) throws Exception {
//        String cols = columnList.stream()
//                .map(s -> "'" + s + "'") // 각 문자열을 작은따옴표로 감쌈
//                .collect(Collectors.joining(", ")); // 쉼표와 공백으로 연결
//        if(columnList.size() == 0) {
//            cols = "''";
//        }
//        
//        String tables = tableList.stream()
//                .map(s -> "'" + s + "'")
//                .collect(Collectors.joining(", "));
//        if(tableList.size() == 0) {
//            tables = "''";
//        }
////        String sql = convertSql(tables, cols, isPk);
//        
//        String template = templateQeury("column_info");
//        Map<String, Object> params = new HashMap<>();
//        params.put("tables", tables);
//        params.put("cols", cols);
//        params.put("pkTag", "N");
//        String sql = freeMarkerQeury(template, params);
//        System.out.println("sql = " + sql);
//        Statement ctmt = con.createStatement();
//        ResultSet rs = ctmt.executeQuery(sql);
//        return convertListMap(rs);
//    }

    public String convertSql(String tables, String cols, boolean isPk) {
        StringBuilder sb = new StringBuilder();
        
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
        if (!isPk) {
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
        return sb.toString();
    }
}
