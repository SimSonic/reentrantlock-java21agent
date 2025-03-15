package ru.simsonic.experiments;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.simsonic.experiments.SynchronizedToReentrantLockContextClassVisitor.OriginalMethodContext;

class SynchronizedToReentrantLockMethodVisitor extends MethodVisitor {

    private final boolean isMethodSynchronized;
    private final int lockStackIndex;
    private final int lockIndex;
    private Label startMethodLabel;

    SynchronizedToReentrantLockMethodVisitor(MethodVisitor mv, OriginalMethodContext originalMethodContext) {
        super(Opcodes.ASM9, mv);
        this.isMethodSynchronized = originalMethodContext.isMethodSynchronized();

        var counter = originalMethodContext.localVariablesCount();
        this.lockStackIndex = counter.getAndIncrement();
        this.lockIndex = counter.getAndIncrement();
    }

    @Override
    public void visitCode() {
        super.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayDeque");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                           "java/util/ArrayDeque",
                           "<init>",
                           "()V",
                           false);
        mv.visitVarInsn(Opcodes.ASTORE, lockStackIndex);

        if (isMethodSynchronized) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            emulateMonitorEnter();
        }

        startMethodLabel = new Label();
        mv.visitLabel(startMethodLabel);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.MONITORENTER) {
            emulateMonitorEnter();
        } else if (opcode == Opcodes.MONITOREXIT) {
            mv.visitInsn(Opcodes.POP);
            emulateMonitorExit();
        } else {
            if (isMethodSynchronized && (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                emulateMonitorExit();
            }
            mv.visitInsn(opcode);
        }
    }

    private void emulateMonitorEnter() {
        // Получаем блокировку для объекта, лежащего на вершине стека.
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                           "ru/simsonic/experiments/SharedReentrantLock",
                           "lock",
                           "(Ljava/lang/Object;)Ljava/util/concurrent/locks/Lock;",
                           false);

        // Добавляем блокировку в стек.
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ASTORE, lockIndex);
        mv.visitVarInsn(Opcodes.ALOAD, lockStackIndex);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                           "java/util/Deque",
                           "push",
                           "(Ljava/lang/Object;)V",
                           true);
    }

    private void emulateMonitorExit() {
        // Извлекаем блокировку из стека.
        mv.visitVarInsn(Opcodes.ALOAD, lockStackIndex);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                           "java/util/Deque",
                           "pop",
                           "()Ljava/lang/Object;",
                           true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/util/concurrent/locks/Lock");

        // Разблокируем.
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                           "java/util/concurrent/locks/Lock",
                           "unlock",
                           "()V",
                           true);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label endMethodLabel = new Label();

        if (isMethodSynchronized) {
            Label handlerLabel = new Label();
            mv.visitTryCatchBlock(startMethodLabel, handlerLabel, handlerLabel, null);

            // Переход к блоку finally в случае исключения.
            mv.visitLabel(handlerLabel);
            emulateMonitorExit();
            mv.visitInsn(Opcodes.ATHROW);

            // Блок finally для нормального завершения метода.
            mv.visitLabel(endMethodLabel);
            emulateMonitorExit();
        } else {
            mv.visitLabel(endMethodLabel);
        }

        mv.visitLocalVariable(
                "__asm_lockStack",
                "Ljava/util/Deque;",
                "Ljava/util/Deque<Ljava/util/concurrent/locks/Lock;>;",
                startMethodLabel,
                endMethodLabel,
                lockStackIndex
        );

        mv.visitLocalVariable(
                "__asm_lock",
                "Ljava/util/concurrent/locks/Lock;",
                null,
                startMethodLabel,
                endMethodLabel,
                lockIndex
        );

        super.visitMaxs(maxStack, maxLocals + 2);
    }
}
