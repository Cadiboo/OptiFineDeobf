package io.github.cadiboo.optifinedeobf.mapping;

import io.github.cadiboo.optifinedeobf.util.Utils;

import java.io.InputStream;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class TSRG2MCP implements MappingService {

	private final HashMap<String, String> fields = new HashMap<>();
	private final HashMap<String, String> methods = new HashMap<>();

	public TSRG2MCP(final InputStream source) {
		String[] lines = Utils.splitNewline(Utils.convertStreamToString(source));
		for (int i = lines.length - 1; i >= 0; --i) {
			String line = lines[i];
			try {
				if (!Character.isWhitespace(line.charAt(0)))
					continue; // We don't care about classes for srg->mcp

				var stripped = line.strip();
				if (Character.isDigit(stripped.charAt(0)))
					continue; // We don't care about parameters
				if (stripped.equals("static"))
					continue; // We don't care if the method is static or not

				var parts = stripped.split(" ");
				if (line.contains("(")) {
					// Method line
					assert parts.length == 3;
					// func_176742_j ()Ljava/lang/String; getName2
					methods.put(parts[0], parts[2]);
				} else {
					// Field line
					assert parts.length == 2;
					// field_82609_l BY_INDEX
					fields.put(parts[0], parts[1]);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing TSRG on line " + i + " \"" + line + "\"", e);
			}
		}
	}

	@Override
	public void dump() {
		this.fields.forEach((raw, mapped) -> System.out.println(raw + " -> " + mapped));
		this.methods.forEach((raw, mapped) -> System.out.println(raw + " -> " + mapped));
	}

	@Override
	public String mapField(String clazz, String name) {
		return fields.getOrDefault(name, name);
	}

	@Override
	public String mapMethod(String clazz, String name, String desc) {
		return methods.getOrDefault(name, name);
	}

}
