package io.github.cadiboo.optifinedeobf.mapping;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

/**
 * @author Cadiboo
 */
public interface MappingService {

	void dump();

	/**
	 * @param clazz The unmapped internal name of the class to remap
	 * @return The mapped internal name of class
	 */
	String mapClass(String clazz);

	/**
	 * @param clazz The unmapped internal name of the field's class
	 * @param name  The unmapped name of the field
	 * @return The mapped name of field
	 */
	String mapField(String clazz, String name);

	/**
	 * @param clazz The unmapped internal name of the method's class
	 * @param name  The unmapped name of the method
	 * @param desc  The unmapped description of the method
	 * @return The mapped name of method
	 */
	String mapMethod(String clazz, String name, String desc);

	default String mapMethodDescriptor(final String desc) {
		Type type = Type.getType(desc);
		StringBuilder stringBuilder = new StringBuilder("(");
		for (final Type arg : type.getArgumentTypes()) {
			appendMappedType(stringBuilder, arg);
		}
		stringBuilder.append(")");
		appendMappedType(stringBuilder, type.getReturnType());
		return stringBuilder.toString();
	}

	default String mapFieldDescriptor(final String desc) {
		StringBuilder stringBuilder = new StringBuilder();
		appendMappedType(stringBuilder, Type.getType(desc));
		return stringBuilder.toString();
	}

	default void appendMappedType(final StringBuilder stringBuilder, final Type type) {
		if (type.getSort() == ARRAY) {
			stringBuilder.append("[");
			appendMappedType(stringBuilder, type.getElementType());
		} else if (type.getSort() == OBJECT) {
			stringBuilder.append("L");
			stringBuilder.append(mapTypeClass(type.getClassName()));
			stringBuilder.append(";");
		} else
			stringBuilder.append(type.toString());
	}

	default String mapTypeClass(String clazz) {
		return mapClass(clazz);
	}

	//TODO
	default String mapClassSignature(String signature) {
		return signature;
	}

	//TODO
	default String mapFieldSignature(String signature) {
		return signature;
	}

	//TODO
	default String mapMethodSignature(String signature) {
		return signature;
	}

	default String mapTypeDescriptor(String desc) {
		StringBuilder stringBuilder = new StringBuilder();
		appendMappedTypeDescriptor(stringBuilder, desc);
		return stringBuilder.toString();
	}

	default void appendMappedTypeDescriptor(final StringBuilder stringBuilder, String desc) {
		if (desc.charAt(0) == '[') {
			stringBuilder.append("[L");
			appendMappedTypeDescriptor(stringBuilder, Type.getType(desc).getElementType().getClassName());
			stringBuilder.append(';');
		} else {
			stringBuilder.append(mapClass(desc));
		}
	}

	boolean needsClassNameRemapping();

}
