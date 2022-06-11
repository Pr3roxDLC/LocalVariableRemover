package org.example.transformer;

import org.example.Utils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM5;


public class LocalVariableReplacer extends Transformer {

    public NamingMode MODE;

    public LocalVariableReplacer(NamingMode namingMode) {
        MODE = namingMode;
    }

    public enum NamingMode {
        UNDERSCORE, //Replaces every local variable name with "_", a reserved keyword
        VAR0, //Replaces every local variable name with var0, Fernflower uses this varX pattern if no local names are available, might confuse some people
        //BYTECODE, //will trick recafs assembler into displaying fake bytecode, (only in older versions) :(
        EMPTY
    }

    @Override
    public HashMap<File, byte[]> transform(HashMap<File, byte[]> byteCollection) {

        HashMap<File, byte[]> outputBytes = new HashMap<>();

        for (Map.Entry<File, byte[]> entry : byteCollection.entrySet()) {

            byte[] bytes = entry.getValue();

            System.out.println("Renaming Local Variables: ");

            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);

            String namePattern = "";
            String parameterNamePattern = "";
            boolean skip = false;

            switch (MODE) {
                case VAR0:
                    namePattern = "var0";
                    parameterNamePattern = "var0";
                    break;
                case UNDERSCORE:
                    namePattern = "_";
                    parameterNamePattern = "_";
                    break;
//            case BYTECODE:
//                //ILOAD d
//                //ILOAD c
//                //IF_ICMPGE A
//                namePattern = "_";
//                parameterNamePattern = "var0";
//                break;
                case EMPTY:
                    namePattern = "\u200B";
                    parameterNamePattern = "\u200B";
                    break;
                default:
                    skip = true;
                    break;
            }
            if (skip) {
                outputBytes.put(entry.getKey(), entry.getValue());
                break;
            }

            String finalNamePattern = namePattern;
            String finalParameterNamePattern = parameterNamePattern;
            ClassVisitor visitor = new ClassVisitor(ASM5, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    System.out.println("Visited: " + name);
                    MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
                    //Count the number of parameters on the method
                    int skip = Utils.getParameterCount(descriptor);
                    final int[] methodsVisited = {0};
                    return new MethodVisitor(ASM5, mv) {
                        @Override
                        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                            methodsVisited[0]++;
                            //if the first var we visit is "this", transform its name, but keep the counter the same
                            if (methodsVisited[0] == 1 && name.equals("this")) {
                                System.out.println("Renamed: " + name);
                                super.visitLocalVariable(finalNamePattern.replace("0", String.valueOf(methodsVisited[0])), descriptor, signature, start, end, index);
                                methodsVisited[0]--;
                                return;
                            }

                            if (skip < methodsVisited[0]) {
                                System.out.println("Renamed: " + name);
                                super.visitLocalVariable(finalNamePattern, descriptor, signature, start, end, index);
                            } else {
                                System.out.println("Kept: " + name);
                                super.visitLocalVariable(finalParameterNamePattern, descriptor, signature, start, end, index);
                            }
                        }
                    };
                }
            };

            reader.accept(visitor, 0);
            outputBytes.put(entry.getKey(), writer.toByteArray());
        }

        return outputBytes;
    }
}
