package com.github.ratelimiter4c;

import com.github.ratelimiter4c.monitor.EventType;
import com.github.ratelimiter4c.monitor.RollingNumber;
import org.junit.Test;

public class RollingNumberTest {

    @Test
    public void test(){
        RollingNumber rollingNumber=new RollingNumber(10000,10);
        rollingNumber.increment(EventType.TOTAL);
    }

    public void parsePath(){
        // /aaa/{123}/bbb
        // /aaa?id=123&name=sss
        // /aaa/{123}

        //
        // appid-ip-path-type-time-count
    }
}
