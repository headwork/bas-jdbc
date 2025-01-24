package com.bas.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class JdbcController {
    private static ConnectionTool ct;
    
    @RequestMapping("/dbConnect")
    public @ResponseBody ResponseEntity<String> dbsettingConnect(@RequestParam Map<String, String> param) {
        if(ct == null) {
            ct = new ConnectionTool();
            ct.setConnection(param.get("dbName"), param);
        }
        List<Map<String, Object>> list = ct.selectExec(param.get("dbName"), param.get("sql"));
        Map<String, Object> resultMap = resultMap(true);
        return toResponseEntity(resultMap);
    }
    
    @RequestMapping("/sqlScriptExec")
    public @ResponseBody ResponseEntity<String> sqlScriptExec(@RequestParam Map<String, String> param) {
        getCt();
        Map<String, Object> resultMap = resultMap(true);
        Map<String, Object> result = ct.selectExecInfo(param.get("dbName"), param.get("sql"));
        resultMap.put("list", result.get("list"));
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
