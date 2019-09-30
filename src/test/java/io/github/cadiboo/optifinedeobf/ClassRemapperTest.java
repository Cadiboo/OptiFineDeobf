package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.H_GETFIELD;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * @author Cadiboo
 */
public class ClassRemapperTest {

	/**
	 * Testing that lambda methods are properly remapped
	 */
	public static void main(String... args) {

		ClassRemapper classRemapper = new ClassRemapper(new TestMappingService(), true, true);
		System.out.println(classRemapper.mapLamdaMethod("", "lambda$func_1112$0", "()V"));
		System.out.println(classRemapper.mapLamdaMethod("", "lambda$func_1111$0", "()V"));
		System.out.println(classRemapper.mapHandleName(new Handle(H_GETFIELD, "", "field_1112", "D", false)));
		System.out.println(classRemapper.mapHandleName(new Handle(H_GETFIELD, "", "field_1111", "D", false)));
		System.out.println(classRemapper.mapHandleName(new Handle(H_INVOKEVIRTUAL, "", "lambda$func_1112$0", "()V", false)));
		System.out.println(classRemapper.mapHandleName(new Handle(H_INVOKEVIRTUAL, "", "lambda$func_1111$0", "()V", false)));

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "testing/Dummy", null, "java/lang/Object", new String[0]);
		// Constructor
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
		}
//		private CompletableFuture<Void> func_213165_a(Runnable p_213165_1_) {
//			return CompletableFuture.supplyAsync(() -> {
//				p_213165_1_.run();
//				return null;
//			}, (Executor)this);
//		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "func_213165_a",
					"(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
					"(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture<Ljava/lang/Void;>;",
					null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInvokeDynamicInsn(
					"get",
					"(Ljava/lang/Runnable;)Ljava/util/function/Supplier;",
					new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
					Type.getType("()Ljava/lang/Object;"),
					new Handle(6, "testing/Dummy", "lambda$func_213165_a$0", "(Ljava/lang/Runnable;)Ljava/lang/Void;", false),
					Type.getType("()Ljava/lang/Void;")
			);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/util/concurrent/CompletableFuture", "supplyAsync", "(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", false);
			mv.visitInsn(ARETURN);
		}
		{
//			private static synthetic lambda$func_213165_a$0(java.lang.Runnable arg0) { //(Ljava/lang/Runnable;)Ljava/lang/Void;
//				 <localVar:index=0 , name=p_213165_1_ , desc=Ljava/lang/Runnable;, sig=null, start=L1, end=L2>
//				 L1 {
//				     aload0
//				     invokeinterface java/lang/Runnable.run()V
//				 }
//				 L3 {
//				     aconst_null
//				     areturn
//				 }
//		     }
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, "lambda$func_213165_a$0", "(Ljava/lang/Runnable;)Ljava/lang/Void;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", false);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
		}
		cw.visitEnd();

		byte[] testClass = cw.toByteArray();

		PrintWriter pw = new PrintWriter(System.out);

		{
			final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(pw);
			new ClassReader(testClass).accept(traceClassVisitor, 0);
		}
		{
			final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(pw);
			new ClassReader(classRemapper.remapClass(testClass)).accept(traceClassVisitor, 0);
		}

	}

	private static class TestMappingService implements MappingService {

		@Override
		public void dump() {
		}

		@Override
		public String mapClass(final String clazz) {
			return clazz;
		}

		@Override
		public String mapField(final String clazz, final String name) {
			if (name.equals("field_1111"))
				return "mappedField";
			return name;
		}

		@Override
		public String mapMethod(final String clazz, final String name, final String desc) {
			if (name.equals("func_1111"))
				return "mappedMethod";
			else if (name.equals("func_213165_a"))
				return "mappedLamdbaMethod";
			return name;
		}

	}

}
