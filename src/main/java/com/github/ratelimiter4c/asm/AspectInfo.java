package com.github.ratelimiter4c.asm;

public class AspectInfo {
    private Class<?> clz;
    private String methodName;
    private String methodDesc;

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    @Override
    public String toString() {
        return "AspectInfo{" +
                "clz=" + clz +
                ", methodName='" + methodName + '\'' +
                ", methodDesc='" + methodDesc + '\'' +
                '}';
    }
}
