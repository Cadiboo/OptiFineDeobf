package io.github.cadiboo.optifinedeobf;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Cadiboo
 */
public class DebugUtils {

	private static final Textifier PRINTER = new Textifier();
	private static final TraceMethodVisitor TRACE_METHOD_VISITOR = new TraceMethodVisitor(PRINTER);

	public static String insnToString(final AbstractInsnNode insn) {
		insn.accept(TRACE_METHOD_VISITOR);
		final StringWriter sw = new StringWriter();
		PRINTER.print(new PrintWriter(sw));
		PRINTER.getText().clear();
		return sw.toString().trim();
	}

	public void printMethod(final MethodNode method) {
		method.instructions.iterator().forEachRemaining(insn -> System.out.println(insnToString(insn)));
	}

}
