package com.github.ratelimiter4c.monitor;

import com.github.ratelimiter4c.db.SaveModel;
import com.github.ratelimiter4c.limiter.rule.source.AppLimitConfig;
import com.github.ratelimiter4c.db.DBUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitorManager {
    private final AppLimitConfig config;
    private final DBUtils dbUtils;
    private final int period = 10;
    private final ScheduledExecutorService scheduledExecutor =
            new ScheduledThreadPoolExecutor(1,r -> new Thread(r, "ratelimiter-monitor-thread"));
    private final Map<String, RollingNumber> cache =new ConcurrentHashMap<>(256);

    public MonitorManager(AppLimitConfig config){
        this.config=config;
        this.dbUtils=new DBUtils(config.getDbUrl(),config.getDbUsername(),config.getDbPassword());
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                report();
            } catch (Exception e) {
                //ignore
            }
        }, period, period, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdown();
            }
        }));
    }

    public void collect(String url, boolean result) {
        RollingNumber rollingNumber=cache.get(url);
        if(rollingNumber==null){
            synchronized(this){
                rollingNumber=cache.get(url);
                if(rollingNumber==null){
                    rollingNumber=new RollingNumber(period,10);
                    cache.put(url,rollingNumber);
                }
            }
        }
        rollingNumber.increment(EventType.TOTAL);
        if (result) {
            rollingNumber.increment(EventType.PASSED);
        } else{
            rollingNumber.increment(EventType.LIMITED);
        }
    }


    private void report() throws Exception{
        Set<String> pathSet=new HashSet<>(cache.keySet());
        if(pathSet.size()==0){
            return;
        }
        List<SaveModel> list=new ArrayList<>(3);
        for(String path:pathSet){
            RollingNumber rollingNumber = cache.get(path);
            if(rollingNumber==null){
                return;
            }
            long total = rollingNumber.getRollingSum(EventType.TOTAL);
            if (total == 0) {
                return;
            }
            long passed = rollingNumber.getRollingSum(EventType.PASSED);
            long limited = rollingNumber.getRollingSum(EventType.LIMITED);
            //todo 入库操作
            Date date=new Date();
            if(total>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.TOTAL.ordinal(),new java.sql.Date(date.getTime()),total));
            }
            if(total>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.PASSED.ordinal(),new java.sql.Date(date.getTime()),passed));
            }
            if(total>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.LIMITED.ordinal(),new java.sql.Date(date.getTime()),limited));
            }
        }
        dbUtils.saveBatch(list);
    }
}
