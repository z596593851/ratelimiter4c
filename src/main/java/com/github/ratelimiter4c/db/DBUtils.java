package com.github.ratelimiter4c.db;

import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.exception.DBException;

import java.sql.*;
import java.util.List;

public class DBUtils {
    private final String url;
    private final String username;
    private final String password;
    private final static String SAVE_SQL="insert into qps_log (appid,url,type,time,count,address) values(?,?,?,?,?,?)";
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DBException(e);
        }
    }

    public DBUtils(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    public void saveBatch(List<SaveModel> models) throws Exception{
        Connection connection=null;
        PreparedStatement ps=null;
        try {
            connection=getConnection();
            ps=connection.prepareStatement(SAVE_SQL);
            for(SaveModel model:models){
                ps.setString(1,model.getAppid());
                ps.setString(2,model.getUrl());
                ps.setInt(3,model.getType());
                ps.setTimestamp(4,new Timestamp(model.getTime().getTime()));
                ps.setLong(5,model.getCount());
                ps.setString(6,model.getAddress());
                ps.addBatch();
            }
            ps.executeBatch();

        } finally {
            if(ps!=null){
                ps.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}
