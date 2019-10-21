package com.server.test.http.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.server.test.http.util.DBUtils.queryDataNoConn;
import static com.server.test.http.util.DBUtils.queryDataNoConnNew;

/**
 * @Author : LeePuvier
 * @CreateTime : 2019/10/21  9:23 PM
 * @ContentUse :
 */
@Slf4j
public class ComUtils {
    public static Map<String, Object> strToMap(String str) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (str.startsWith("{") && str.endsWith("}")) {
            str = str.substring(1, str.length());
            str = str.substring(0, str.length() - 1);
            String[] eArr = str.split("=");
            String key = "";
            for (int i = 0; i < eArr.length; i++) {
                String tempStr = eArr[i];
                //如果为最后一个直接做为值进行封装
                if (i == eArr.length - 1) {
                    map.put(key, tempStr.replace(" ", ""));
                } else {
                    //检查字符串中是否包含又"'{', '}','[',']'"字符
                    if ((tempStr).contains("{") || (tempStr).contains("[") || (tempStr).contains("}") || (tempStr).contains("]")) {
                        Stack<String> stackChar = new Stack<String>();
                        Integer stackLength = null;
                        for (int j = 0; j < tempStr.length(); j++) {
                            char c = tempStr.charAt(j);
                            if ((c + "").equals("{") || (c + "").equals("[")) {
                                stackChar.push(c + "");
                                stackLength = stackChar.size();
                            } else if ((c + "").equals("}") || (c + "").equals("]")) {
                                stackChar.pop();
                                stackLength = stackChar.size();
                            } else if ((c + "").equals(",")) {
                                if (stackLength == 0) {
                                    //跳出该循环，并从该处进行分离
                                    String jsonStr = tempStr.substring(0, j);
                                    String newKey = tempStr.substring(j + 1, tempStr.length());
                                    map.put(key, jsonStr.replace(" ", ""));
                                    key = newKey.replace(" ", "");
                                    //清空栈
                                    stackChar.clear();
                                    break;
                                }
                            }
                        }
                    } else {
                        //检查是否有逗号
                        if (tempStr.contains(",")) {
                            Stack<String> stack = new Stack<String>();
                            //从分离的字符串中获取上一个key的value和下一个key的name
                            for (int j = 0; j < tempStr.length(); j++) {
                                char c = tempStr.charAt(j);
                                if (!(c + "").equals(",")) {
                                    stack.push(c + "");
                                } else if ((c + "").equals(" ")) {
                                    continue;
                                } else {
                                    String sStr = stack.pop();
                                    if (sStr.equals("\"")) {
                                        stack.push(sStr);
                                        stack.push(c + "");
                                    } else {
                                        String jsonStr = tempStr.substring(0, j);
                                        String newKey = tempStr.substring(j + 1, tempStr.length());
                                        map.put(key, jsonStr.replace(" ", ""));
                                        key = newKey.replace(" ", "");
                                        stack.clear();
                                        break;
                                    }
                                }
                            }
                        } else {
                            key = tempStr.replace(" ", "");
                        }
                    }
                }
            }
        } else {
            log.info("不是正确的Map格式");
        }
        return map;
    }
    //list
    public   static boolean listCompare(List<String> list1, List<String> list2) {
        if(list1.size()!=list2.size()){
            log.info("两个List的Size不一致");
            log.info(String.valueOf(list1.size()));
            log.info(String.valueOf(list2.size()));
            return false;
        }else {
            for(String item:list1){
                if(!list2.contains(item)){
                    return false;
                }
            }
        }
        return true;
    }
    //set
    public   static boolean setCompare(Set<String> set1, Set<String> set2) {
        if(set1.size()!=set2.size()){
            return false;
        }else {
            for(String item:set2){
                if(!set1.contains(item)){
                    return false;
                }
            }
        }
        return true;
    }
    //json对比
    public static boolean jsonCompare(JSONObject json1, JSONObject json2){
        try {
            Set<String> json1Keys = json1.keySet();
            Set<String> json2Keys = json2.keySet();
            if(!setCompare(json1Keys,json2Keys)){
                log.info("长度不一致");
                return false;
            }else {
                for (String item:json1Keys){
                    if(!json1.get(item).toString().equals(json2.get(item).toString())){
                        log.info(json1.get(item).toString());
                        log.info(json2.get(item).toString());
                        return false;
                    }
                }
            }
        }catch (Exception e){
            log.info("抛异常了");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //json对比，except中的内容与接口返回至对比，非全量
    public static boolean jsonCompairExpect(JSONObject response,JSONObject expect) {
        try {
            if (response==null&&expect==null)
                return true;
            Set<String> responseKeys = response.keySet();
            Set<String> exceptKeys = expect.keySet();

            if (responseKeys.size()==exceptKeys.size()&&response.size()==0)
            {
                return true;
            }
            for (String key : exceptKeys) {
                if (!responseKeys.contains(key)) {
                    return false;
                }
            }
            for (String key : exceptKeys) {
                System.out.println(key);
                if (!response.get(key).toString().equals(expect.get(key).toString())) {
                    return false;
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }




    // 对比mysql数据库
    public static Boolean compareMysqlData(JSONObject mysqlData, String specialSql) throws SQLException {
        List<String> keyList = new ArrayList<>();
        String mysqlField = "";
        for (String key:mysqlData.keySet()){
            keyList.add(key);
            mysqlField = mysqlField+key+",";
        }

        String sql = specialSql.replace("$field$",mysqlField.substring(0,mysqlField.length() - 1));

        log.info("sql语句："+sql);

        ResultSet resultSet = queryDataNoConn(sql);
        if(null != resultSet && !"".equals(resultSet)){
            while (resultSet.next()){
                for (String key:mysqlData.keySet()) {
                    String expectedValue = mysqlData.get(key).toString();
                    String mysqlValue = resultSet.getString(keyList.indexOf(key)+1);
                    if(!expectedValue.equals(mysqlValue)){
                        log.info("expectedValue:"+expectedValue);
                        log.info("mysqlValue:"+mysqlValue);
                        return false;
                    }
                }
                return true;
            }
        }else {
            log.info("数据库无记录");
            return false;
        }
        return false;
    }


    // 对比mysql数据库
    public static Boolean compareMysqlDataNew(JSONObject mysqlData, String specialSql) throws SQLException {
        Set<String> dataKeySet = mysqlData.keySet();
        String mysqlField = "";
        for (String key:dataKeySet){
            mysqlField = mysqlField+key+",";
        }

        String sql = specialSql.replace("$field$",mysqlField.substring(0,mysqlField.length() - 1));

        log.info("sql语句："+sql);

        ResultSet resultSet = queryDataNoConnNew(sql);
        if(null != resultSet && !"".equals(resultSet)){
            while (resultSet.next()){
                for (String key:dataKeySet) {
                    String expectedValue = mysqlData.getString(key);
                    String newMysqlValue = resultSet.getString(key);
                    if(!expectedValue.equals(newMysqlValue)){
                        log.info("expectedValue:"+expectedValue);
                        log.info("mysqlValue:"+newMysqlValue);
                        return false;
                    }
                }
                return true;
            }
        }else {
            log.info("数据库无记录");
            return false;
        }
        return false;
    }

    // 精确校验异常信息
    // expectedMsg中的所有key和value必须完全相等
    public static boolean judgeExceptionPrecise(String exceptionStr,JSONObject expectedMsg){
        // 按照 "BusinessException" 分割异常信息，并去除两边的可能存在的 "{" 和 "}"
        String msg = exceptionStr.split("BusinessException")[1].replace("{", "").replace("}", "");

        // 按照 "," 分割 异常的信息
        String[] msgList = msg.split(",");

        boolean flag = true;
        for (String kv : msgList) {
            String[] ll = kv.split("=");
            String key = ll[0].replace("'", "");
            String value = ll[1].replace("'", "");
            if(!expectedMsg.get(key).equals(value)){
                flag = false;
                log.info("在key为 "+ key+" 处，value值不等");
                log.info("预期value："+ expectedMsg.get(key));
                log.info("实际value："+ value);
                return flag;
            }
        }
        return flag;
    }


    // 粗略校验异常信息
    // expectedMsg中的key为"code" 对应的value必须完全相等
    // 其余的key对应的value只要 包含 在实际信息中即可
    public static boolean judgeExceptionRough(String exceptionStr,JSONObject expectedMsg){
        // 按照 "BusinessException" 分割异常信息，并去除两边的可能存在的 "{" 和 "}"
        String msg = exceptionStr.replace("{", "").replace("}", "");

        // 按照 "," 分割 异常的信息
        String[] msgList = msg.split(",",2);

        boolean flag = true;
        for (String kv : msgList) {
            String[] ll = kv.split("=");
            String key = ll[0].replace("'", "");
            String value = ll[1].replace("'", "");
            if(!value.contains(expectedMsg.getString(key))){
                flag = false;
                log.info("在key为 '"+ key+"' 处，value值不等");
                log.info("预期value："+ expectedMsg.get(key));
                log.info("实际value："+ value);
                return flag;
            }else{
                if("code".equals(key)){
                    if(!expectedMsg.get(key).equals(value)){
                        flag = false;
                        log.info("code校验不相等");
                        log.info("预期code："+ expectedMsg.get(key));
                        log.info("实际code："+ value);
                        return flag;
                    }
                }
            }
        }
        return flag;
    }
}
