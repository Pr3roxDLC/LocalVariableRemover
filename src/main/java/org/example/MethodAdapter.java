package org.example;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodAdapter extends MethodVisitor {

    int i = 0;
    String zwwsc="_";
    public MethodAdapter(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
                                   Label start, Label end, int index) {
        System.out.println("Visited: " + name);
        super.visitLocalVariable( zwwsc, desc, signature, start, end, index);
    }


}
