package com.github.ratelimiter4c.asm.adapter;


import com.github.ratelimiter4c.asm.annotation.AsmAspect;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

@AsmAspect(className = "javax/servlet/http/HttpServlet",
        method = {"service (Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V"})
public class HttpServletMethodVisitor extends AdviceAdapter {


    public HttpServletMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
        super(ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getRequestURI", "()Ljava/lang/String;", true);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "javax/servlet/http/HttpServlet", "limiter", "Lcom/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter;");
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "com/github/ratelimiter4c/limiter/rule/MemoryUrlRateLimiter", "limit", "(Ljava/lang/String;)Z", false);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitVarInsn(ILOAD, 4);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("has been limited");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


        mv.visitVarInsn(ALOAD, 2);
        mv.visitLdcInsn("utf-8");
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletResponse", "setCharacterEncoding", "(Ljava/lang/String;)V", true);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitLdcInsn("application/json; charset=utf-8");
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletResponse", "setContentType", "(Ljava/lang/String;)V", true);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletResponse", "getWriter", "()Ljava/io/PrintWriter;", true);
        mv.visitVarInsn(ASTORE, 5);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitLdcInsn("has been limited");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintWriter", "write", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintWriter", "flush", "()V", false);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintWriter", "close", "()V", false);

        mv.visitInsn(RETURN);
        mv.visitLabel(l0);

//        loadArg(1);
//        invokeInterface(Type.getType(HttpServletRequest.class), new Method("getRequestURI","()Ljava/lang/String;"));
//        int path=newLocal(Type.LONG_TYPE);
//        storeLocal(path);
//        getStatic(Type.getType(System.class),"out",Type.getType(PrintStream.class));
//        loadLocal(path);
//        invokeVirtual(Type.getType(PrintStream.class),new Method("println","(Ljava/lang/String;)V"));
//        loadThis();
//        getField(Type.getType(HttpServlet.class),"limiter",Type.getType(MemoryUrlRateLimiter.class));
//        loadLocal(path);
//        invokeVirtual(Type.getType(MemoryUrlRateLimiter.class),new Method("limit","(Ljava/lang/String;)Z"));
//        int result=newLocal(Type.LONG_TYPE);
//        storeLocal(result);
//        loadLocal(result);
//        Label ifLable=newLabel();
//        visitJumpInsn(Opcodes.IFNE,ifLable);
//        getStatic(Type.getType(System.class),"out",Type.getType(PrintStream.class));
//        visitLdcInsn("has been limited");
//        invokeVirtual(Type.getType(PrintStream.class),new Method("println","(Ljava/lang/String;)V"));
//        returnValue();
//        visitLabel(ifLable);


    }


    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals+2);
    }
}
