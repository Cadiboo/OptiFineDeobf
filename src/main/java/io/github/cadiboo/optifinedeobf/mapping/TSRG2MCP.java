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
				if (!Character.isWhitespace(line.charAt(0))) {
					// NOP, we don't care about classes for srg->mcp
				} else if (line.contains("(")) {
					// Method line
					// func_176742_j ()Ljava/lang/String; getName2
					final String[] s = line.trim().split(" ");
					methods.put(s[0], s[2]);
				} else {
					// Field line
					// field_82609_l BY_INDEX
					final String[] s = line.trim().split(" ");
					fields.put(s[0], s[1]);
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
