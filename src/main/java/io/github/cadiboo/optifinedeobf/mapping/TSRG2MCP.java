package io.github.cadiboo.optifinedeobf.mapping;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Cadiboo
 */
public class TSRG2MCP implements MappingService {

	public static final String DEFAULT_MAPPINGS_FILE = "srg_to_snapshot_20190922-1.14.3.tsrg";

	private final HashMap<String, String> fields = new HashMap<>();
	private final HashMap<String, String> methods = new HashMap<>();

	public TSRG2MCP(final InputStream source) {
		try (Scanner scanner = new Scanner(source, StandardCharsets.UTF_8.name())) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (!line.startsWith(" ") && !line.startsWith("\t")) {
					// Class line
					continue;
				}
				if (line.contains("(")) {
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
			}
		}
	}

	public TSRG2MCP() {
		this(TSRG2MCP.class.getResourceAsStream("/" + DEFAULT_MAPPINGS_FILE));
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
