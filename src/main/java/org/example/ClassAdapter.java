package org.example;

import org.objectweb.asm.*;

public class ClassAdapter extends ClassVisitor {
    boolean visitedFirstPrivateField = false;
    public ClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new MethodAdapter(mv);
    }


//    @Override
//    public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value){
//        if(access == Opcodes.ACC_PRIVATE && !visitedFirstPrivateField) {
//            visitedFirstPrivateField = true;
//            FieldVisitor fv = cv.visitField(access, "_", descriptor, signature, value);
//            return new FieldAdapter(fv);
//        }else{
//            FieldVisitor fv = cv.visitField(access, name, descriptor, signature, value);
//            return new FieldAdapter(fv);
//        }
//
//    }

}

