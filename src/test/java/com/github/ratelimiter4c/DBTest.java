package com.github.ratelimiter4c;

import com.github.ratelimiter4c.db.DBUtils;
import com.github.ratelimiter4c.db.SaveModel;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;

public class DBTest {

    @Test
    public void insert() throws Exception {
        DBUtils dbUtils=new DBUtils("jdbc:mysql://127.0.0.1:3306/local","root","root");
        SaveModel model=new SaveModel("app1","/tqr",0,new Date(),1L,"");
        dbUtils.saveBatch(Collections.singletonList(model));
    }

    @Test
    public void timeTest() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println(addr.getHostAddress());
    }

}
