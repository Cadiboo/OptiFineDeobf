package io.github.cadiboo.optifinedeobf.mapping;

import io.github.cadiboo.optifinedeobf.util.Utils;

import java.io.InputStream;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class SRG2MCP implements MappingService {

	private final HashMap<String, String> fields = new HashMap<>();
	private final HashMap<String, String> methods = new HashMap<>();

	public SRG2MCP(final InputStream source) {
		String[] lines = Utils.splitNewline(Utils.convertStreamToString(source));
		for (int i = lines.length - 1; i >= 0; --i) {
			String line = lines[i];
			try {
				if (line.startsWith("CL:")) {
					// NOP, we don't care about classes for srg->mcp
				} else if (line.startsWith("FD:")) {
					// FD: net/minecraft/world/storage/loot/functions/Smelt/field_186574_a net/minecraft/world/storage/loot/functions/Smelt/LOGGER
					final String[] s = line.split(" ");
					final String[] raw = s[1].split("/");
					final String[] mapped = s[2].split("/");
					fields.put(raw[raw.length - 1], mapped[mapped.length - 1]);
				} else if (line.startsWith("MD:")) {
					// MD: net/minecraft/block/Block/func_208619_r ()Z net/minecraft/block/Block/isVariableOpacity ()Z
					final String[] s = line.split(" ");
					final String[] raw = s[1].split("/");
					final String[] mapped = s[3].split("/");
					methods.put(raw[raw.length - 1], mapped[mapped.length - 1]);
				} else {
					System.out.println("Unknown SRG mappings on line " + i + " \"" + line + "\"");
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing SRG on line " + i + " \"" + line + "\"", e);
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
