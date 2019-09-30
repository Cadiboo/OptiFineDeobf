package io.github.cadiboo.optifinedeobf.mapping;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

/**
 * @author Cadiboo
 */
public interface MappingService {

	void dump();

	String mapClass(String clazz);

	String mapField(String clazz, String name);

	String mapMethod(String clazz, String name, String desc);

	default String mapDesc(final String desc) {
		Type type = Type.getType(desc);
		StringBuilder stringBuilder = new StringBuilder("(");
		for (final Type arg : type.getArgumentTypes()) {
			appendMappedType(stringBuilder, arg);
		}
		stringBuilder.append(")");
		appendMappedType(stringBuilder, type.getReturnType());
		return stringBuilder.toString();
	}

	default void appendMappedType(final StringBuilder stringBuilder, final Type type) {
		if (type.getSort() == ARRAY) {
			stringBuilder.append("[");
			stringBuilder.append(mapTypeClass(type.getElementType().getClassName()));
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

}
