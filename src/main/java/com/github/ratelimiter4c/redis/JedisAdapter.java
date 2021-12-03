package com.github.ratelimiter4c.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Collections;

public class JedisAdapter {
    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);
    private final JedisPool pool;
    private final String url;

    public JedisAdapter(String url){
        this.url=url;
//        this.pool=null;
        this.pool = new JedisPool(url);
    }

    public long set(String key, String value) {
        //add to set
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String get(String key) {
        //add to set
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Object eval(String luaScript, String key, String...param) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.eval(luaScript, Collections.singletonList(key), Arrays.asList(param));
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


}
