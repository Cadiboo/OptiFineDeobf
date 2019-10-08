package io.github.cadiboo.optifinedeobf.mapping;

import org.objectweb.asm.ClassReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
			if (map == null) {
				if (mapped.superclass != null) {
					HashMap<String, String> superMap = mapped.superclass.methods.get(name);
					if (superMap != null) {
						String s = superMap.get(desc);
						if (s != null)
							return s;
					}
					for (final TSRGClass anInterface : mapped.interfaces) {
						HashMap<String, String> interfaceMap = anInterface.methods.get(name);
						if (interfaceMap != null) {
							String s2 = interfaceMap.get(desc);
							if (s2 != null)
								return s2;
						}
					}
				}
				return name;
			} else {
				String s = map.get(desc);
				if (s != null)
					return s;
				for (final TSRGClass anInterface : mapped.interfaces) {
					HashMap<String, String> interfaceMap = anInterface.methods.get(name);
					if (interfaceMap != null) {
						String s2 = interfaceMap.get(desc);
						if (s2 != null) return s2;
					}
				}
				return name;
			}
		}
		return name;
	}

	@Override
	public boolean needsClassNameRemapping() {
		return true;
	}

	@Override
	public boolean wantsSuperclassMap() {
		return true;
	}

	@Override
	public void buildSuperclassMap(byte[] clazz) {
		ClassReader classReader = new ClassReader(clazz);
		TSRGClass tsrgClass = classes.get(classReader.getClassName());
		if (tsrgClass != null) {
			String superName = classReader.getSuperName();
			if (superName != null && !superName.equals("java/lang/Object")) {
				TSRGClass tsrgSuperclass = classes.get(superName);
				if (tsrgSuperclass != null)
					tsrgClass.superclass = tsrgSuperclass;
			}
			for (final String anInterface : classReader.getInterfaces()) {
				TSRGClass tsrgInterface = classes.get(anInterface);
				if (tsrgInterface != null)
					tsrgClass.interfaces.add(tsrgInterface);
			}
		}
	}

	private class TSRGClass {

		private final String mappedName;
		// unmapedName -> mappedName
		private final HashMap<String, String> fields = new HashMap<>();
		// unmapedName -> descriptor -> mappedName
		private final HashMap<String, HashMap<String, String>> methods = new HashMap<>();

		TSRGClass superclass;
		List<TSRGClass> interfaces = new ArrayList<>(0);

		private TSRGClass(final String mappedName) {
			this.mappedName = mappedName;
		}

	}

}
