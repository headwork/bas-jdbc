package com.bas.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlQueryMariadbImpl extends SqlQueryAbstract {
    public SqlQueryMariadbImpl(ConnectionVo cv) {
        super(cv);
    }
}
