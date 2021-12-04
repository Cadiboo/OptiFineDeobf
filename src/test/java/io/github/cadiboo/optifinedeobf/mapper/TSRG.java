package io.github.cadiboo.optifinedeobf.mapper;

import io.github.cadiboo.optifinedeobf.util.Utils;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cadiboo
 */
public class TSRG implements Mapper {

	private final ArrayList<MappingsClass> classes = new ArrayList<>();

	private void appendMappedType(final StringBuilder stringBuilder, final Type type, final HashMap<String, String> obf2mappedClasses) {
		if (type.getSort() == Type.ARRAY)
			appendMappedType(stringBuilder, Type.getType(type.getDescriptor().substring(1)), obf2mappedClasses);
		else if (type.getSort() == Type.OBJECT) {
			final String internalName = type.getInternalName();
			stringBuilder.append(obf2mappedClasses.getOrDefault(internalName, internalName));
		} else // primitive
			stringBuilder.append(type.getDescriptor());
	}

	@Override
	public List<MappingsClass> getClasses() {
		return classes;
	}

	public TSRG parse(final InputStream source) {
		String[] lines = Utils.splitNewline(Utils.convertStreamToString(source));

		// First pass - collect classes (obf -> mapped)
		final var obf2mappedClasses = new HashMap<String, String>();
		for (int i = lines.length - 1; i >= 0; --i) {
			var line = lines[i];
			try {
				if (!Character.isWhitespace(line.charAt(0))) {
					//a net/minecraft/client/renderer/Quaternion
					var split = line.split(" ");
					obf2mappedClasses.put(split[0], split[1]);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing TSRG class on line " + i + " \"" + line + "\"", e);
			}
		}

		// Second pass - parse everything
		MappingsClass currentClass = null;
		for (int i = 0; i < lines.length; ++i) {
			var line = lines[i];
			try {
				if (!Character.isWhitespace(line.charAt(0))) {
					//a net/minecraft/client/renderer/Quaternion
					var split = line.split(" ");
					classes.add(currentClass = new MappingsClass(split[0], split[1]));
				} else if (line.contains("(")) {
					// Method line
					// a ()F func_195889_a
					var split = line.trim().split(" ");
//
					var obfParams = Type.getArgumentTypes(split[1]);

					var mappedDesc = new StringBuilder("(");
					for (var param : obfParams)
						appendMappedType(mappedDesc, param, obf2mappedClasses);
					mappedDesc.append(")");

					currentClass.methods.add(new MappingsMethod(split[0], split[2], split[1], mappedDesc.toString()));
				} else {
					// Field line
					// a field_195895_a
					var s = line.trim().split(" ");
					currentClass.fields.add(new MappingsField(s[0], s[1], "", ""));
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing TSRG on line " + i + " \"" + line + "\"", e);
			}
		}
		return this;
	}

}
