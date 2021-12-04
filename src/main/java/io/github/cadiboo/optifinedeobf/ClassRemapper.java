package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Cadiboo
 */
public class ClassRemapper {

	public final MappingService mappingService;
	public final boolean makePublic;
	public final boolean definalise;

	ClassRemapper(MappingService mappingService, boolean makePublic, boolean definalise) {
		this.mappingService = mappingService;
		this.makePublic = makePublic;
		this.definalise = definalise;
	}

	byte[] remapClass(byte[] inputClass) {
		var classNode = new ClassNode(ASM9);
		var classReader = new ClassReader(inputClass);
		classReader.accept(classNode, 0);

		classNode.access = correctAccess(classNode.access);

		for (var innerClassNode : classNode.innerClasses)
			innerClassNode.access = correctAccess(innerClassNode.access);

		for (var field : classNode.fields) {
			field.access = correctAccess(field.access);
			field.name = mappingService.mapField(classNode.name, field.name);
		}

		for (var method : classNode.methods) {
			if ((makePublic || definalise) && !method.name.equals("<clinit>"))
				method.access = correctAccess(method.access);
			method.name = mapLambdaMethod(classNode.name, method.name, method.desc);

			method.instructions.iterator().forEachRemaining(insn -> {
				if (insn instanceof FieldInsnNode fieldInsn) {
					fieldInsn.name = mappingService.mapField(fieldInsn.owner, fieldInsn.name);
				} else if (insn instanceof MethodInsnNode methodInsn) {
					methodInsn.name = mapLambdaMethod(methodInsn.owner, methodInsn.name, methodInsn.desc);
				} else if (insn instanceof InvokeDynamicInsnNode) {
					var bsmArgs = ((InvokeDynamicInsnNode) insn).bsmArgs;
					for (int i = 0; i < bsmArgs.length; ++i) {
						if (bsmArgs[i] instanceof Handle handle) {
							String mappedName = mapHandleName(handle);
							if (!mappedName.equals(handle.getName()))
								bsmArgs[i] = new Handle(handle.getTag(), handle.getOwner(), mappedName, handle.getDesc(), handle.isInterface());
						}
					}
				}
			});
		}

		var classWriter = new ClassWriter(ASM9);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

	int correctAccess(int access) {
		if (makePublic) {
			// Remove all access (mask it away)
			access &= ~(ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE);
			// Add public access
			access |= ACC_PUBLIC;
		}
		if (definalise) {
			// Mask off final
			access &= ~ACC_FINAL;
		}
		return access;
	}

	String mapHandleName(Handle handle) {
		return isField(handle) ? mappingService.mapField(handle.getOwner(), handle.getName()) : mapLambdaMethod(handle.getOwner(), handle.getName(), handle.getDesc());
	}

	private static boolean isField(Handle handle) {
//		Fields: H_GETFIELD H_GETSTATIC H_PUTFIELD H_PUTSTATIC
//		Methods: H_INVOKEVIRTUAL H_INVOKESTATIC H_INVOKESPECIAL H_NEWINVOKESPECIAL H_INVOKEINTERFACE
		return handle.getTag() < H_INVOKEVIRTUAL;
	}

	/**
	 * Remaps lambda method names like `lambda$func_1111$0` to `lambda$mappedName$0`
	 */
	String mapLambdaMethod(final String owner, final String name, final String desc) {
		if (!name.startsWith("lambda$"))
			return mappingService.mapMethod(owner, name, desc);
		// Handle nested lamdbas
		int start$ = name.indexOf('$') + 1;
		int last$ = name.lastIndexOf('$');

		// Apparently "lambda$0" is a valid method name... what?
		if (start$ - 1 == last$)
			return name;

		return name.substring(0, start$) + mappingService.mapMethod(owner, name.substring(start$, last$), desc) + name.substring(last$);
	}

}
