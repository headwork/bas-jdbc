package com.bas.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@CrossOrigin(origins = "*")
public class JdbcController {
    private static ConnectionTool ct;
    
    @RequestMapping("/dbConnect")
    public @ResponseBody ResponseEntity<String> dbsettingConnect(@RequestParam Map<String, String> param) {
        String dbName = param.get("dbName");
        if(ct == null) {
            ct = new ConnectionTool();
            ct.setConnection(dbName, param);
        }else {
            if(!ct.isConnect(dbName)) {
                ct.setConnection(dbName, param);
            }
        }
        SqlQuery sq = ct.getSqlQuery(dbName);
        List<Map<String, Object>> list = sq.selectExec(param.get("sql"));
        Map<String, Object> resultMap = resultMap(true);
        return toResponseEntity(resultMap);
    }
    
    @RequestMapping("/sqlScriptExec")
    public @ResponseBody ResponseEntity<String> sqlScriptExec(@RequestParam Map<String, String> param) {
        getCt();
        String dbName = param.get("dbName");
        boolean isConnect = ct.isConnect(dbName);
        Map<String, Object> resultMap = resultMap(isConnect);
        if(!isConnect) {
            resultMap.put("resultMsg", "접속된 Connection 정보가 없습니다.");
        }
        SqlQuery sq = ct.getSqlQuery(dbName);
        Map<String, Object> result = sq.selectExecInfo(param.get("sql"));
        resultMap.put("list", result.get("list"));
        resultMap.put("columnInfo", result.get("columnInfo"));
        return toResponseEntity(resultMap);
    }

    @RequestMapping("/database/{page}")
    public @ResponseBody ResponseEntity<String> tableList(@PathVariable String page, @RequestParam Map<String, Object> param) {
        getCt();
        String dbName = (String)param.get("dbName");
        boolean isConnect = ct.isConnect(dbName);
        Map<String, Object> resultMap = resultMap(isConnect);
        if(!isConnect) {
            resultMap.put("resultMsg", "접속된 Connection 정보가 없습니다.");
        }
        SqlQuery sq = ct.getSqlQuery(dbName);
        
        Map<String, Object> result = null;
        if("tableList".equals(page)) {
            result = sq.tableList(param);
            resultMap.put("list", result.get("list"));
        }else if("tableInfo".equals(page)) {
            result = sq.tableInfo(param);
            resultMap.put("list", result.get("list"));
        }else if("databaseList".equals(page)) {
            result = sq.databaseList();
            resultMap.put("list", result.get("list"));
        }

        resultMap.put("columnInfo", result.get("columnInfo"));
        return toResponseEntity(resultMap);
    }

    public static ConnectionTool getCt() {
        if(ct == null) {
            ct = new ConnectionTool();
        }
        return ct;
    }
    public ResponseEntity<String> toResponseEntity(Object obj) {
        Gson gson = new GsonBuilder().setLenient().create();
        return new ResponseEntity<String>(gson.toJson(obj), HttpStatus.OK);
    }
    
    @ExceptionHandler()
    public ResponseEntity<String> handleException(Throwable e) {
        if(e != null) e.printStackTrace();
//        return new ResponseEntity(HttpStatus.BAD_REQUEST);
        return toResponseEntity(resultMap(false));
    }

    public Map<String, Object> resultMap(boolean isSuccess) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(isSuccess) {
            resultMap.put("resultCode", 1);
            resultMap.put("resultMsg", "정상으로 처리됐습니다.");
        }else {
            resultMap.put("resultCode", -1);
            resultMap.put("resultMsg", "처리중 오류가 발생했습니다. 관리자에게 문의하세요");
        }
        return resultMap;
    }
}
