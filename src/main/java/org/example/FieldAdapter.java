package org.example;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class FieldAdapter extends FieldVisitor {
    protected FieldAdapter(FieldVisitor fieldVisitor) {
        super(Opcodes.ASM5, fieldVisitor);
    }
}
