package com.bas.jdbc;

public class Util {
    public static String convertQueryParam(Object param) {
        return convertQueryParam((String)param, "");
    }
    public static String convertQueryParam(String param) {
        return convertQueryParam(param, "");
    }
    public static String convertQueryParam(String param, String def) {
        return param == null ? def : param.replaceAll("'", "''");
    }
}
