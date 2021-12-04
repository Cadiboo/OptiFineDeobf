package io.github.cadiboo.optifinedeobf.mapper;

import io.github.cadiboo.optifinedeobf.util.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cadiboo
 */
public class SRG implements Mapper {

	private final ArrayList<MappingsClass> classes = new ArrayList<>();

	@Override
	public List<MappingsClass> getClasses() {
		return classes;
	}

	public SRG parse(InputStream source) {
		var lines = Utils.splitNewline(Utils.convertStreamToString(source));

		// First pass - collect classes (obf -> mappingsClass)
		var obf2mappingsClasses = new HashMap<String, MappingsClass>();
		for (int i = lines.length - 1; i >= 0; --i) {
			String line = lines[i];
			try {
				if (line.startsWith("CL:")) {
					//CL: a net/minecraft/client/renderer/Quaternion
					var split = line.split(" ");
					var obfName = split[1];
					obf2mappingsClasses.put(obfName, new MappingsClass(obfName, split[2]));
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing SRG class on line " + i + " \"" + line + "\"", e);
			}
		}

		for (int i = lines.length - 1; i >= 0; --i) {
			String line = lines[i];
			try {
				if (line.startsWith("CL:")) {
					// NOP, we've already done classes
				} else if (line.startsWith("FD:")) {
					//FD: net/minecraft/world/storage/loot/functions/Smelt/field_186574_a net/minecraft/world/storage/loot/functions/Smelt/LOGGER
					var split = line.split(" ");
					var obf = split[1];
					var mapped = split[2];

					var obfLastSlash = obf.lastIndexOf('/');

					var obfClass = obf.substring(0, obfLastSlash);
					var obfName = obf.substring(obfLastSlash + 1);

					var mappedName = mapped.substring(mapped.lastIndexOf('/') + 1);

					obf2mappingsClasses.get(obfClass).fields.add(new MappingsField(obfName, mappedName, "", ""));
				} else if (line.startsWith("MD:")) {
					//MD: eap/a (Lcxq;)V net/minecraft/client/audio/SoundHandler/func_215289_a (net/minecraft/client/renderer/ActiveRenderInfo)
					var split = line.split(" ");
					var obf = split[1];
					var mapped = split[3];

					var obfLastSlash = obf.lastIndexOf('/');

					var obfClass = obf.substring(0, obfLastSlash);
					var obfName = obf.substring(obfLastSlash + 1);

					var mappedName = mapped.substring(mapped.lastIndexOf('/') + 1);

					obf2mappingsClasses.get(obfClass).methods.add(new MappingsMethod(obfName, mappedName, split[2], split[4]));
				} else {
					System.out.println("Unknown SRG mappings on line " + i + " \"" + line + "\"");
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed parsing SRG on line " + i + " \"" + line + "\"", e);
			}
		}

		classes.addAll(obf2mappingsClasses.values());
		return this;
	}

}
