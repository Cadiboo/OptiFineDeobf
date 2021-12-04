package io.github.cadiboo.optifinedeobf.mapper;

import io.github.cadiboo.optifinedeobf.util.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public class Proguard implements Mapper {

	private static final Predicate<String> COMMENT = s -> s.startsWith("#");

	private final ArrayList<MappingsClass> classes = new ArrayList<>();

	private String getInternalClassName(final String canonicalClassName) {
		return canonicalClassName.replace('.', '/');
	}

	@Override
	public List<MappingsClass> getClasses() {
		return classes;
	}

	@Override
	public Proguard parse(final InputStream source) {
		String[] lines = Utils.splitNewline(Utils.convertStreamToString(source));
		lines = Arrays.stream(lines)
				.filter(COMMENT.negate())
				.toArray(String[]::new);

		// First pass - collect classes (mapped -> obf)
		final HashMap<String, String> mapped2obfClasses = new HashMap<>();
		for (int i = lines.length - 1; i >= 0; --i) {
			String line = lines[i];
			try {
				if (!Character.isWhitespace(line.charAt(0))) {
					//net.minecraft.world.entity.animal.horse.AbstractHorse -> ate:
					final String[] split = line.split(" ");
					String obfName = split[2];
					obfName = obfName.substring(0, obfName.indexOf(':'));
					mapped2obfClasses.put(split[0], obfName);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing Proguard class on line " + i + " \"" + line + "\"", e);
			}
		}

		// Second pass - parse everything
		MappingsClass currentClass = null;
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			try {
				if (!Character.isWhitespace(line.charAt(0))) {
					//net.minecraft.world.entity.animal.horse.AbstractHorse -> ate:
					final String[] split = line.split(" ");
					String obfName = split[2];
					obfName = obfName.substring(0, obfName.indexOf(':'));
					classes.add(currentClass = new MappingsClass(getInternalClassName(obfName), getInternalClassName(split[0])));
				} else if (line.contains("(")) {
					// Method line
					//354:411:boolean mobInteract(net.minecraft.world.entity.player.Player,net.minecraft.world.InteractionHand) -> a
					final String[] split = line.trim().split(" ");
					String[] methodSplit = split[1].split("\\(");
					String mappedName = methodSplit[0];

					final String[] colonSplit = split[0].split(":");
					final String descReturn = colonSplit[colonSplit.length - 1];

					final String methodStr = methodSplit[1];
					String[] descParams = methodStr.substring(0, methodStr.indexOf(')')).split(",");

					StringBuilder obfDesc = new StringBuilder("(");
					for (String param : descParams) {
						if (isPrimitive(param)) obfDesc.append(getInternalName(param));
						else obfDesc.append(getInternalName(mapped2obfClasses.getOrDefault(param, param)));
					}
					obfDesc.append(")");
					if (isPrimitive(descReturn)) obfDesc.append(getInternalName(descReturn));
					else obfDesc.append(getInternalName(mapped2obfClasses.getOrDefault(descReturn, descReturn)));

					StringBuilder mappedDesc = new StringBuilder("(");
					for (String param : descParams)
						mappedDesc.append(getInternalName(param));
					mappedDesc.append(")");
					mappedDesc.append(getInternalName(descReturn));

					currentClass.methods.add(new MappingsMethod(split[3], mappedName, obfDesc.toString(), mappedDesc.toString()));
				} else {
					// Field line
					//java.util.function.Predicate PREY_SELECTOR -> bD
					final String[] split = line.trim().split(" ");

					String desc = split[0];

					String obfDesc;
					if (isPrimitive(desc)) obfDesc = getInternalName(desc);
					else obfDesc = getInternalName(mapped2obfClasses.getOrDefault(desc, desc));

					String mappedDesc = getInternalName(desc);

					currentClass.fields.add(new MappingsField(split[3], split[1], obfDesc, mappedDesc));
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing Proguard on line " + i + " \"" + line + "\"", e);
			}
		}
		return this;
	}

	private String getInternalName(String obj) {
		if (obj.isEmpty()) return obj;
		return switch (obj) {
			case "int" -> "I";
			case "float" -> "F";
			case "double" -> "D";
			case "long" -> "J";
			case "boolean" -> "Z";
			case "short" -> "S";
			case "byte" -> "B";
			case "void" -> "V";
			default -> "L" + getInternalClassName(obj) + ";";
		};
	}

	private boolean isPrimitive(String obj) {
		return switch (obj) {
			case "int", "float", "double", "long", "boolean", "short", "byte", "void" -> true;
			default -> false;
		};
	}

}
