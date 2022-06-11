package org.example.transformer;

import org.apache.commons.lang3.tuple.Pair;
import org.example.Utils;
import org.objectweb.asm.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM5;

public class MethodRenamer extends Transformer {

    public static HashMap<String, HashMap<String, String>> methodMap = new HashMap<>();

    public enum RenamingMode {

    }

    @Override
    public HashMap<File, byte[]> transform(HashMap<File, byte[]> byteCollection){

        HashMap<File, byte[]> outputBytes = new HashMap<>();

        for(Map.Entry<File, byte[]> entry : byteCollection.entrySet()) {
            byte[] bytes = entry.getValue();
            System.out.println("Renaming Methods: ");

            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);

            methodMap.put(reader.getClassName(), new HashMap<>());

            ClassVisitor visitor = new ClassVisitor(ASM5, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    System.out.println("Renamed method: " + name);
                    if (name.contains("<")) {
                        return cv.visitMethod(access, name, descriptor, signature, exceptions);
                    } else {
                        return cv.visitMethod(access, name + "renamed", descriptor, signature, exceptions);
                    }
                }
            };

//        @Override
//        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//            System.out.println("method: " + reader.getClassName() + ":" + name  + descriptor);
//            super.visitMethodInsn(opcode, owner, name+"renamed"+descriptor, descriptor, isInterface);
//        }

            reader.accept(visitor, 0);

            outputBytes.put(entry.getKey(), writer.toByteArray());

        }

        return outputBytes;

    }
}
