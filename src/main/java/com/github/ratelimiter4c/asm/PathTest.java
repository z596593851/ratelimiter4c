package com.github.ratelimiter4c.asm;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathTest {
    public String resovlePackagePath(String webPackage) {
        // 扫码所有的包并把其放入到访问关系和方法放入到内存中
        File f = new File(getClass().getResource("/").getPath());
        String totalPath = f.getAbsolutePath();
        System.out.println("totalPath:"+totalPath);
        String packagePath = webPackage.replace(".", "/");
        totalPath = totalPath + "/" + packagePath;
        return totalPath;
    }

    /**
     * 解析包下面的所有类
     * @param packagePath 上一步获取包的全路径
     * @param webPackage  包(cn.ishow.test)
     * @return
     */
    public List<String> parseClassName(String packagePath, String webPackage) {
        List<String> array = new ArrayList<>();
        File root = new File(packagePath);
        resolveFile(root, webPackage, array);
        return array;

    }

    public List<String> parseClass(String packagePath){
        File f = new File(getClass().getResource("/").getPath());
        String totalPath = f.getAbsolutePath();
        System.out.println("totalPath:"+totalPath);
        packagePath = packagePath.replace(".", "/");
        totalPath = totalPath + "/" + packagePath;
        List<String> list = new ArrayList<>();
        File root = new File(totalPath);
        resolveFile(root, packagePath, list);
        return list;
    }

    private void resolveFile(File root, String webPackage, List<String> classNames) {
        if (!root.exists()) {
            return;
        }
        File[] childs = root.listFiles();
        if (childs != null && childs.length > 0) {
            for (File child : childs) {
                if (child.isDirectory()) {
                    String parentPath = child.getParent();
                    String childPath = child.getAbsolutePath();
                    String temp = childPath.replace(parentPath, "");
                    temp = temp.replace("/", "");
                    resolveFile(child, webPackage + "." + temp, classNames);
                } else {
                    String fileName = child.getName();
                    if (fileName.endsWith(".class")) {
                        String name = fileName.replace(".class", "");
                        String className = webPackage + "." + name;
                        classNames.add(className);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        PathTest pathTest=new PathTest();
        String packag="com.hxm.ratelimiter.asm.adapter";
        String totalPath = pathTest.resovlePackagePath(packag);
        List<String> list = pathTest.parseClassName(totalPath, packag);
        System.out.println(JSONObject.toJSONString(list));
    }

}
