package com.github.ratelimiter4c.monitor;

import com.github.ratelimiter4c.constant.ValueConstant;
import com.github.ratelimiter4c.db.DBUtils;
import com.github.ratelimiter4c.db.SaveModel;
import com.github.ratelimiter4c.limiter.rule.source.AppLimitConfig;
import com.github.ratelimiter4c.utils.Utils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitorManager {
    private final AppLimitConfig config;
    private final DBUtils dbUtils;

    private final ScheduledExecutorService scheduledExecutor =
            new ScheduledThreadPoolExecutor(1,r -> new Thread(r, "ratelimiter-monitor-thread"));
    /**
     * qps缓存
     */
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
        }, ValueConstant.MONITOR_REPORT_PERIOD_MS, ValueConstant.MONITOR_REPORT_PERIOD_MS, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdown();
            }
        }));
    }

    public void collect(String url, boolean result) {
        //todo 各接口总访问量
        RollingNumber rollingNumber=cache.get(url);
        if(rollingNumber==null){
            synchronized(this){
                rollingNumber=cache.get(url);
                if(rollingNumber==null){
                    rollingNumber=new RollingNumber(ValueConstant.MONITOR_RN_STATISTIC_PERIOD_MS,ValueConstant.MONITOR_RN_NUMBER_OF_BUCKETS);
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
        //todo 统计总访问量、异常量、400/500量、访问耗时

        //todo 定时汇总做成发布订阅模式
        //todo 降级：a调b，可以在a里主动降级b或者b的某个函数
        //todo 熔断：a调b，可以在a里配置调b时的熔断条件，一旦触发就自动降级

        //todo 也可以在a设置降级策略，当a服务中某个接口的qps、异常量、400/500量、访问耗时
        // 达到阈值时，直接返回某个结果。
        // 当设置了限流时，就不要再设置qps的降级策略了，两者冲突

        //todo 也可以直接降级某个接口
        System.out.println("定时任务-"+Thread.currentThread().getName());
        Set<String> pathSet=new HashSet<>(cache.keySet());
        if(pathSet.size()==0){
            return;
        }
        for(String path:pathSet){
            RollingNumber rollingNumber = cache.get(path);
            if(rollingNumber==null){
                continue;
            }
            long total = rollingNumber.getRollingSum(EventType.TOTAL);
            if (total == 0) {
                continue;
            }
            System.out.println("path:"+path+"-"+"total:"+total);
            List<SaveModel> list=new ArrayList<>(3);
            long passed = rollingNumber.getRollingSum(EventType.PASSED);
            long limited = rollingNumber.getRollingSum(EventType.LIMITED);
            // 入库操作
            Date date=new Date();
            String address= Utils.getHostAddress();
            if(total>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.TOTAL.ordinal(),new java.sql.Date(date.getTime()),total,address));
            }
            if(passed>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.PASSED.ordinal(),new java.sql.Date(date.getTime()),passed,address));
            }
            if(limited>0){
                list.add(new SaveModel(config.getAppId(),path,EventType.LIMITED.ordinal(),new java.sql.Date(date.getTime()),limited,address));
            }
            if(!list.isEmpty()){
                dbUtils.saveBatch(list);
                System.out.println("入库");
            }
        }

    }
}
