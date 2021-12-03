package com.github.ratelimiter4c.asm;


import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.utils.Wildcard;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

public class MyClassFileTransformer implements ClassFileTransformer {

    private final Map<String, List<AspectInfo>> config;

    public MyClassFileTransformer(Map<String, List<AspectInfo>> config) {
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        if (config.containsKey(className)) {
            System.out.println("增强"+className);
            ClassReader cr = new ClassReader(classBytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new MyClassVisitor(cw, className,config);
            cr.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            return cw.toByteArray();
        }

        return classBytes;
    }

    public static class MyClassVisitor extends ClassVisitor {
        private final String className;
        private final ClassVisitor cv;
        private final Map<String, List<AspectInfo>> config;

        public MyClassVisitor(ClassVisitor cv, String className,Map<String, List<AspectInfo>> config) {
            super(Opcodes.ASM5, cv);
            this.cv=cv;
            this.className = className;
            this.config=config;
        }


        @Override
        public MethodVisitor visitMethod(int access, String methodName, String methodDesc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, methodName, methodDesc, signature, exceptions);
            List<AspectInfo> aspectInfos=this.config.get(className);
            if(aspectInfos==null){
                return mv;
            }
            for (AspectInfo item : aspectInfos) {
                String aspectMethodName = item.getMethodName();
                String aspectMethodDesc = item.getMethodDesc();
                if (!Wildcard.equalsOrMatch(methodName, aspectMethodName) || !Wildcard.equalsOrMatch(methodDesc, aspectMethodDesc)) {
                    continue;
                }
                System.out.println(item.getMethodName());
                Class<?> clz = item.getClz();
                try {
                    return (AdviceAdapter)ConstructorUtils.invokeConstructor(clz, mv, access, methodName, methodDesc);
                } catch (Exception e) {
                    throw new AsmException(e);
                }
            }
            return mv;
        }

        @Override
        public void visitEnd() {
            FieldVisitor fv=cv.visitField(0, "limiter", "Lcom/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter;", null, null);
            fv.visitEnd();
        }
    }
}
