package com.github.ratelimiter4c.db;

import com.github.ratelimiter4c.exception.AsmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DBUtils {
    private final String url;
    private final String username;
    private final String password;
    private final static String SAVE_SQL="insert into qps_log (appid,url,type,time,count) values(?,?,?,?,?)";
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new AsmException(e);
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
                ps.setDate(4,model.getTime());
                ps.setLong(5,model.getCount());
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
