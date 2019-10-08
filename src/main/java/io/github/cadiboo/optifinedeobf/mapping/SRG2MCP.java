package io.github.cadiboo.optifinedeobf.mapping;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Cadiboo
 */
public class SRG2MCP implements MappingService {

	private final HashMap<String, String> fields = new HashMap<>();
	private final HashMap<String, String> methods = new HashMap<>();

	public SRG2MCP() {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream("/srg2mcp.srg"), StandardCharsets.UTF_8.name())) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (line.startsWith("FD") && line.contains("field")) {
					// FD: net/minecraft/world/storage/loot/functions/Smelt/field_186574_a net/minecraft/world/storage/loot/functions/Smelt/LOGGER
					final String[] s = line.split(" ");
					final String[] raw = s[1].split("/");
					final String[] mapped = s[2].split("/");
					fields.put(raw[raw.length - 1], mapped[mapped.length - 1]);
				} else if (line.startsWith("MD") && line.contains("func")) {
					// MD: net/minecraft/block/Block/func_208619_r ()Z net/minecraft/block/Block/isVariableOpacity ()Z
					final String[] s = line.split(" ");
					final String[] raw = s[1].split("/");
					final String[] mapped = s[3].split("/");
					methods.put(raw[raw.length - 1], mapped[mapped.length - 1]);
				}
			}
		}
	}

	@Override
	public void dump() {
		this.fields.forEach((raw, mapped) -> System.out.println(raw + " -> " + mapped));
		this.methods.forEach((raw, mapped) -> System.out.println(raw + " -> " + mapped));
	}

	@Override
	public String mapClass(final String clazz) {
		return clazz;
	}

	@Override
	public String mapField(String clazz, String name) {
		return fields.getOrDefault(name, name);
	}

	@Override
	public String mapMethod(String clazz, String name, String desc) {
		return methods.getOrDefault(name, name);
	}

	@Override
	public boolean needsClassNameRemapping() {
		return false;
	}

	@Override
	public boolean wantsSuperclassMap() {
		return false;
	}

}
