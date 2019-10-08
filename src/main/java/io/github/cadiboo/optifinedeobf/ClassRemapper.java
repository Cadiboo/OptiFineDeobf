package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;

/**
 * @author Cadiboo
 */
public class ClassRemapper {

	public final MappingService mappingService;
	public final boolean makePublic;
	public final boolean definalise;

	ClassRemapper(final MappingService mappingService, final boolean makePublic, final boolean definalise) {
		this.mappingService = mappingService;
		this.makePublic = makePublic;
		this.definalise = definalise;
	}

	byte[] remapClass(byte[] inputClass) {
		final ClassNode classNode = new ClassNode(ASM5);
		final ClassReader classReader = new ClassReader(inputClass);
		classReader.accept(classNode, 0);

		final boolean needsClassNameRemapping = mappingService.needsClassNameRemapping();

		final String unmappedClassName = classNode.name;

		classNode.access = correctAccess(classNode.access);
		classNode.name = mappingService.mapClass(classNode.name);
		if (classNode.signature != null)
			classNode.signature = mappingService.mapClassSignature(classNode.signature);
		if (classNode.superName != null)
			classNode.superName = mappingService.mapClass(classNode.superName);
		final List<String> interfaces = classNode.interfaces;
		for (int i = 0, interfacesSize = interfaces.size(); i < interfacesSize; ++i) {
			interfaces.set(i, mappingService.mapClass(interfaces.get(i)));
		}
		for (InnerClassNode innerClassNode : classNode.innerClasses) {
			innerClassNode.access = correctAccess(innerClassNode.access);
			innerClassNode.name = mappingService.mapClass(innerClassNode.name);
			innerClassNode.outerName = classNode.name;
			innerClassNode.innerName = innerClassNode.name.replace(classNode.name, "");
		}

		for (final FieldNode field : classNode.fields) {
			field.access = correctAccess(field.access);
			field.name = mappingService.mapField(unmappedClassName, field.name);
			if (needsClassNameRemapping) {
				field.desc = mappingService.mapFieldDescriptor(field.desc);
				if (field.signature != null)
					field.signature = mappingService.mapFieldSignature(field.signature);
			}
		}

		for (final MethodNode method : classNode.methods) {
			if ((makePublic || definalise) && !method.name.equals("<clinit>"))
				method.access = correctAccess(method.access);
			method.name = mapLamdaMethod(unmappedClassName, method.name, method.desc);
			if (needsClassNameRemapping) {
				method.desc = mappingService.mapMethodDescriptor(method.desc);
				if (method.signature != null)
					method.signature = mappingService.mapMethodSignature(method.signature);
			}
			method.instructions.iterator().forEachRemaining(insn -> {
				if (insn instanceof TypeInsnNode) {
					TypeInsnNode typeInsn = (TypeInsnNode) insn;
					if (needsClassNameRemapping) {
						typeInsn.desc = mappingService.mapTypeDescriptor(typeInsn.desc);
					}
				} else if (insn instanceof FieldInsnNode) {
					final FieldInsnNode fieldInsn = (FieldInsnNode) insn;
					fieldInsn.name = mappingService.mapField(fieldInsn.owner, fieldInsn.name);
					if (needsClassNameRemapping) {
						fieldInsn.desc = mappingService.mapFieldDescriptor(fieldInsn.desc);
						fieldInsn.owner = mappingService.mapClass(fieldInsn.owner);
					}
				} else if (insn instanceof MethodInsnNode) {
					final MethodInsnNode methodInsn = (MethodInsnNode) insn;
					methodInsn.name = mapLamdaMethod(methodInsn.owner, methodInsn.name, methodInsn.desc);
					if (needsClassNameRemapping) {
						methodInsn.desc = mappingService.mapMethodDescriptor(methodInsn.desc);
						methodInsn.owner = mappingService.mapClass(methodInsn.owner);
					}
				} else if (insn instanceof InvokeDynamicInsnNode) {
					final Object[] bsmArgs = ((InvokeDynamicInsnNode) insn).bsmArgs;
					for (int i = 0; i < bsmArgs.length; ++i) {
						final Object bsmArg = bsmArgs[i];
						if (bsmArg instanceof Handle) {
							Handle handle = (Handle) bsmArg;
							String mappedName = mapHandleName(handle);
							if (needsClassNameRemapping || !mappedName.equals(handle.getName()))
								if (needsClassNameRemapping) {
									int tag = handle.getTag();
									if (tag < H_INVOKEVIRTUAL) // Field
										bsmArgs[i] = new Handle(tag, mappingService.mapClass(handle.getOwner()), mappedName, mappingService.mapFieldDescriptor(handle.getDesc()), handle.isInterface());
									else
										bsmArgs[i] = new Handle(tag, mappingService.mapClass(handle.getOwner()), mappedName, mappingService.mapMethodDescriptor(handle.getDesc()), handle.isInterface());
								} else
									bsmArgs[i] = new Handle(handle.getTag(), handle.getOwner(), mappedName, handle.getDesc(), handle.isInterface());
						}
					}
				}
			});
			if (method.localVariables != null)
				method.localVariables.forEach(localVariableNode -> {
					if (needsClassNameRemapping) {
//						localVariableNode.name = mappingService.mapParameter(localVariableNode.name);
						localVariableNode.desc = mappingService.mapFieldDescriptor(localVariableNode.desc);
						localVariableNode.signature = mappingService.mapFieldSignature(localVariableNode.signature);
					}
				});
		}

		final ClassWriter classWriter = new ClassWriter(ASM5);
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

	String mapHandleName(final Handle handle) {
		int tag = handle.getTag();
//		Fields: H_GETFIELD H_GETSTATIC H_PUTFIELD H_PUTSTATIC
//		Methods: H_INVOKEVIRTUAL H_INVOKESTATIC H_INVOKESPECIAL H_NEWINVOKESPECIAL H_INVOKEINTERFACE
		if (tag < H_INVOKEVIRTUAL) // Field
			return mappingService.mapField(handle.getOwner(), handle.getName());
		else {
			return mapLamdaMethod(handle.getOwner(), handle.getName(), handle.getDesc());
		}
	}

	/**
	 * Remaps lambda method names like `lambda$func_1111$0` to `lambda$mappedName$0`
	 */
	String mapLamdaMethod(final String owner, final String name, final String desc) {
		if (name.startsWith("lambda$")) { // Handle nested lamdbas
			int start$ = name.indexOf('$') + 1;
			int last$ = name.lastIndexOf('$');
			return name.substring(0, start$) + mappingService.mapMethod(owner, name.substring(start$, last$), desc) + name.substring(last$);
		} else
			return mappingService.mapMethod(owner, name, desc);
	}

}
