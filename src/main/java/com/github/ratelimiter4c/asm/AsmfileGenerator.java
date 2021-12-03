package com.github.ratelimiter4c.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.PrintWriter;

public class AsmfileGenerator {
    public static void main(final String[] args) throws Exception {
        ClassReader cr = new ClassReader(new FileInputStream("/Users/apple/code/java/ratelimiter4c/target/classes/com/github/ratelimiter4c/asm/AsmTemplate.class"));
        cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(
                System.out)), ClassReader.SKIP_DEBUG);
    }
}
