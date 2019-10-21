package com.server.test.http.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : LeePuvier
 * @CreateTime : 2019/10/21  9:20 PM
 * @ContentUse : 数据库基本配置与基本操作
 */
@Slf4j
public class DBUtils {
    private static String   driver="com.mysql.jdbc.Driver";
    private static String url="jdbc:mysql://10.41.95.205:3306/capital_testing11?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
    private static String user="root";
    private static String pwd="@0cYt7Yd9m4nmaMrjMXPu4h@OlmOQh";
    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnection()  {
        try {
            return DriverManager.getConnection(url,user,pwd);
        } catch (SQLException e) {
            log.info("db连接异常,请检查!");
            e.printStackTrace();
        }
        return null;
    }
    //  新增、修改、删除
    public  static int addOrUpdateOrDel(String sql)  {
        Statement st = null;
        try {
            st = getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int i=0;
        try {
            i = st.executeUpdate(sql);
        }catch (RuntimeException e){
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }
    //新增ID
    public static int insertAndGetId(String sql){
        int id=-1;
        try {
            PreparedStatement pst=getConnection().prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            pst.executeUpdate();
            ResultSet rs=pst.getGeneratedKeys();
            if(rs.next()){
                id=rs.getInt(1);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    //  查询
    public static ResultSet queryData(Connection conn,String sql) throws SQLException {
        ResultSet rs = null;
        try {
            rs = conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    //  查询 使用PreparedStatement
    public static ResultSet queryDatawithPrepareStmt(Connection conn,String sql) throws SQLException {
        ResultSet rs = null;
        try {
            rs = conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    // 获取结果集
    public static List getQueryResultList(Connection conn, String sql) throws SQLException{
        ResultSet rs = null;
        List list = new ArrayList();
        try {
            rs = conn.createStatement().executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map rowData = new HashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));//获取键名及值
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.info("查询数据库异常,请检查!");
            log.info(sql);
            e.printStackTrace();
        }
        return list;
    }
    //清理数据
    public static boolean clearData(String vars){
        boolean flag=false;
        if(!"".equals(vars)||vars.length()!=0){
            String[] clearSqls = vars.split(";");
            for (String clearSql : clearSqls) {
                if(1==addOrUpdateOrDel(clearSql)){
                    flag=true;
                }
            }
        }else {
            flag=true;
        }
        return flag;
    }
    //初始化数据
    public static int initData(String vars,boolean isGenKey){
        int result=-1;
        if(!"".equals(vars)||vars.length()!=0){
            String[] pareSqls = vars.split(";");
            for (String pareSql : pareSqls) {
                if(isGenKey){
                    result=insertAndGetId(pareSql);
                }else {
                    result=addOrUpdateOrDel(pareSql);
                }
            }
        }else {
            result =1;
        }
        return result;
    }
    //初始化数据
    public static boolean initData(String vars){
        boolean flag=false;
        if(!"".equals(vars)||vars.length()!=0){
            String[] pareSqls = vars.split(";");
            for (String pareSql : pareSqls) {
                if(1==addOrUpdateOrDel(pareSql)){
                    flag=true;
                }else {
                    log.info("当前初始化数据sql执行失败:"+pareSql);
                }
            }
        }else {
            flag=true;
        }
        return flag;
    }

    // 查询数据
    public static ResultSet queryDataNoConn(String sql) throws SQLException {
        ResultSet rs;
        if (null != sql && !"".equals(sql)) {
            Connection connection = DBUtils.getConnection();
            rs = DBUtils.queryData(connection, sql);
            return rs;
        } else {
            return null;
        }
    }


    // 查询数据使用prepareStament
    public static ResultSet queryDataNoConnNew(String sql) throws SQLException {
        ResultSet rs;
        if (null != sql && !"".equals(sql)) {
            Connection connection = DBUtils.getConnection();
            rs = DBUtils.queryDatawithPrepareStmt(connection, sql);
            return rs;
        } else {
            return null;
        }
    }


    public static void close(Statement stmt){
        if(stmt != null){
            try{
                stmt.close();
            }catch(SQLException e){
                e.printStackTrace();
            }

        }
    }

    public static void close(PreparedStatement pstmt){
        if(pstmt != null){
            try{
                pstmt.close();
            }catch(SQLException e){
                e.printStackTrace();
            }

        }
    }

    public static void close(Connection conn){
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs){
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}