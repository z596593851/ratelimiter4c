package com.github.ratelimiter4c.asm.adapter;

import com.github.ratelimiter4c.asm.annotation.AsmAspect;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import javax.servlet.http.HttpServlet;

@AsmAspect(className = "javax/servlet/http/HttpServlet", method = {"<init> ()V"})
public class InitMethodVisitor extends AdviceAdapter {

    public InitMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
        super(ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, "com/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "com/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter", "<init>", "()V", false);
        mv.visitFieldInsn(PUTFIELD, "javax/servlet/http/HttpServlet", "limiter", "Lcom/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter;");
        mv.visitInsn(RETURN);

//        loadThis();
//        newInstance(Type.getType(MemoryUrlRateLimiter.class));
//        dup();
//        invokeConstructor(Type.getType(MemoryUrlRateLimiter.class),new Method("<init>", "()V"));
//        putField(Type.getType(HttpServlet.class),"limiter",Type.getType(MemoryUrlRateLimiter.class));
//        returnValue();

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack+3, maxLocals);
    }

    public static void main(String[] args) {
        System.out.println(Type.getType(HttpServlet.class).getInternalName());
    }
}
