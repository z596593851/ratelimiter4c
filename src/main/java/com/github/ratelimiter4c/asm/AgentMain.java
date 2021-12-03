package com.github.ratelimiter4c.asm;

import com.alibaba.fastjson.JSONObject;

import com.github.ratelimiter4c.asm.adapter.HttpServletMethodVisitor;
import com.github.ratelimiter4c.asm.adapter.InitMethodVisitor;
import com.github.ratelimiter4c.asm.annotation.AsmAspect;
import com.github.ratelimiter4c.exception.AsmException;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentMain {

    private static final List<Class<? extends AdviceAdapter>> visitorList=new ArrayList<>();
    static {
        visitorList.add(InitMethodVisitor.class);
        visitorList.add(HttpServletMethodVisitor.class);
    }

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        Map<String, List<AspectInfo>> config = initConfig();
        instrumentation.addTransformer(new MyClassFileTransformer(config), true);
    }

    private static Map<String,List<AspectInfo>> initConfig() {
        try {
            Map<String,List<AspectInfo>> config=new HashMap<>(16);
            for(Class<? extends AdviceAdapter> clazz:visitorList){
                List<AspectInfo> aspectInfos=new ArrayList<>();
                AsmAspect anno = clazz.getAnnotation(AsmAspect.class);
                if(anno!=null){
                    String className = anno.className();
                    String[] methodList = anno.method();
                    for (String item : methodList) {
                        AspectInfo aspectInfo = new AspectInfo();
                        aspectInfo.setClz(clazz);
                        String[] split = item.split(" ");
                        if (split.length == 2) {
                            aspectInfo.setMethodName(split[0]);
                            aspectInfo.setMethodDesc(split[1]);
                        }
                        if (split.length == 1) {
                            aspectInfo.setMethodName(split[0]);
                            aspectInfo.setMethodDesc("*");
                        }
                        aspectInfos.add(aspectInfo);
                    }
                    if(config.containsKey(className)){
                        List<AspectInfo> list = config.get(className);
                        list.addAll(aspectInfos);
                    }else{
                        config.put(className,aspectInfos);
                    }

                }
            }
            System.out.println(JSONObject.toJSONString(config));
            return config;
        } catch (Exception e) {
            throw new AsmException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        initConfig();
    }
}
