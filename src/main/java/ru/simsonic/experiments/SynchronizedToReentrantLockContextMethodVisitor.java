package ru.simsonic.experiments;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.simsonic.experiments.SynchronizedToReentrantLockContextClassVisitor.OriginalMethodContext;

class SynchronizedToReentrantLockContextMethodVisitor extends MethodVisitor {

    private final OriginalMethodContext context;

    SynchronizedToReentrantLockContextMethodVisitor(OriginalMethodContext context) {
        super(Opcodes.ASM9);
        this.context = context;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.MONITORENTER) {
            context.monitorCount().incrementAndGet();
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        context.localVariablesCount().set(maxLocals);
    }
}
