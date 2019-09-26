package compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import parsermanual.Node;
import parsermanual.ParseTree;

import static java.lang.Integer.max;

public class BytecodeCompiler implements Opcodes {

    public byte[] compile(ParseTree tree) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(V1_8, ACC_PUBLIC, "Out", null, "java/lang/Object", null);

        // Create a constructor
//        MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//        constructor.visitCode();
//        constructor.visitVarInsn(ALOAD, 0);
//        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//        constructor.visitInsn(RETURN);
//        constructor.visitMaxs(1, 1);

        // Generate the main method
        MethodVisitor main = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, new String[]{"java/io/IOException"});
        main.visitCode();

        // Load 0 into variable 1 (the 'buffer')
        // TODO what happens if this is commented out?
        main.visitInsn(ICONST_0);
        main.visitVarInsn(ISTORE, 1);

        int stack = 0;
        int maxStack = 0;

        Node current = tree.root;
        while (current != null) {
//            switch(current.token.type) {
//                case READ:
                    main.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
                    main.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "()I", false);
                    stack++;
                    maxStack = max(stack, maxStack);

                    main.visitInsn(I2B);
                    main.visitVarInsn(ISTORE, 1);
                    stack--;
//                    break;
//                case WRITE:
                    main.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    main.visitVarInsn(ILOAD, 1);
                    stack += 2;
                    maxStack = max(stack, maxStack);

                    main.visitInsn(I2C);
                    main.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V", false);
                    stack -= 2;
//                    break;
//                case PUSH:
                    main.visitVarInsn(ILOAD, 1);
                    stack++;
                    maxStack = max(stack, maxStack);
//                    break;
//                case POP:
                    main.visitVarInsn(ISTORE, 1);
                    stack--;
//                    break;
            }
            current = current.right;
//        }

        main.visitInsn(RETURN);
        main.visitMaxs(maxStack, 2);

        writer.visitEnd();

        return writer.toByteArray();
    }
}
