package io.github.cadiboo.optifinedeobf.mapping;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Cadiboo
 */
public class Obf2SRG implements MappingService {

	private final HashMap<String, TSRGClass> classes = new HashMap<>();

	public Obf2SRG() {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream("/obf2srg.tsrg"), StandardCharsets.UTF_8.name())) {
			TSRGClass clazz = null;
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (!line.startsWith(" ") && !line.startsWith("\t")) {
					// Class line
					// net/minecraft/client/Minecraft net/minecraft/client/Minecraft
					final String[] s = line.trim().split(" ");
					clazz = new TSRGClass(s[1]);
					classes.put(s[0], clazz);
				} else if (line.contains("(")) {
					// Method line
					// func_176742_j ()Ljava/lang/String; getName2
					final String[] s = line.trim().split(" ");
					clazz.methods.computeIfAbsent(s[0], k -> new HashMap<>()).put(s[1], s[2]);
				} else {
					// Field line
					// field_82609_l BY_INDEX
					final String[] s = line.trim().split(" ");
					clazz.fields.put(s[0], s[1]);
				}
			}
		}
	}

	@Override
	public void dump() {
		this.classes.forEach((rawClass, mappedClass) -> {
			String mappedClassName = mappedClass.mappedName;
			System.out.println(rawClass + " -> " + mappedClassName);
			mappedClass.fields.forEach((raw, mapped) -> System.out.println(rawClass + "." + raw + " -> " + mappedClassName + "." + mapped));
			mappedClass.methods.forEach((raw, descMap) -> descMap.forEach((desc, mapped) -> System.out.println(rawClass + "." + raw + " -> " + mappedClassName + "." + mapped + desc)));
		});
	}

	@Override
	public String mapClass(final String clazz) {
		TSRGClass mapped = classes.get(clazz);
		if (mapped != null) {
			return mapped.mappedName;
		}
		return clazz;
	}

	@Override
	public String mapField(String clazz, String name) {
		TSRGClass mapped = classes.get(clazz);
		if (mapped != null) {
			return mapped.fields.getOrDefault(name, name);
		}
		return name;
	}

	@Override
	public String mapMethod(String clazz, String name, String desc) {
		TSRGClass mapped = classes.get(clazz);
		if (mapped != null) {
			HashMap<String, String> map = mapped.methods.get(name);
			if (map == null)
				return name;
			return map.getOrDefault(desc, name);
		}
		return name;
	}

	@Override
	public boolean needsClassNameRemapping() {
		return true;
	}

	private class TSRGClass {

		private final String mappedName;
		// unmapedName -> mappedName
		private final HashMap<String, String> fields = new HashMap<>();
		// unmapedName -> descriptor -> mappedName
		private final HashMap<String, HashMap<String, String>> methods = new HashMap<>();

		private TSRGClass(final String mappedName) {
			this.mappedName = mappedName;
		}

	}

}
