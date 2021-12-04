package io.github.cadiboo.optifinedeobf.mapper;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Cadiboo
 */
public interface Mapper {

	record MappingsField(
		String obfName,
		String mappedName,
		String obfDesc,
		String mappedDesc
	) {
	}

	record MappingsMethod(
		String obfName,
		String mappedName,
		String obfDesc,
		String mappedDesc
	) {
	}

	class MappingsClass {

		public final String obfName;
		public final String mappedName;
		public final ArrayList<MappingsField> fields = new ArrayList<>();
		public final ArrayList<MappingsMethod> methods = new ArrayList<>();

		public MappingsClass(final String obfName, final String mappedName) {
			this.obfName = obfName;
			this.mappedName = mappedName;
		}

	}

	static void main(String... args) throws IOException {
		convert("/Users/Cadiboo/Desktop/client.txt", Printer.TSRG);
		convert("/Users/Cadiboo/Desktop/client.txt", Printer.SRG);
		convert("/Users/Cadiboo/Desktop/obf2srg.tsrg", Printer.SRG);
		convert("/Users/Cadiboo/Desktop/srg_to_snapshot_20190922-1.14.3.srg", Printer.TSRG);
		new Proguard().parse(new FileInputStream("/Users/Cadiboo/Desktop/client.txt")).toSRG(System.out);
	}

	static void convert(String name, Printer printer) throws IOException {
		var file = new File(name);
		var fileName = file.getName();

		final Mapper mapper;
		if (fileName.endsWith(".txt"))
			mapper = new Proguard();
		else if (fileName.endsWith(".srg"))
			mapper = new SRG();
		else if (fileName.endsWith(".tsrg"))
			mapper = new TSRG();
		else
			throw new IllegalStateException("Unsupported mappings extension");

		try (var source = new FileInputStream(file)) {
			mapper.parse(source);
		}
		try (
			var out = new FileOutputStream(Paths.get(file.toPath() + printer.extension).toFile());
			var printStream = new PrintStream(out)
		) {
			printer.consumer.accept(mapper, printStream);
		}

	}

	static void print(final PrintStream out, ArrayList<String> strings) {
		for (final String s : strings) out.println(s);
	}

	List<MappingsClass> getClasses();

	default void toSRG(PrintStream out) {
		var classes = getClasses();

		var srgClasses = new ArrayList<String>();
		var srgFields = new ArrayList<String>();
		var srgMethods = new ArrayList<String>();

		for (var mappingsClass : classes) {
			var classObfName = mappingsClass.obfName;
			var classMappedName = mappingsClass.mappedName;

			srgClasses.add("CL: " + classObfName + " " + classMappedName);

			for (var mappingsField : mappingsClass.fields) {
//				srgFields.add("FD: " + classObfName + "/" + mappingsField.obfName + " " + mappingsField.obfDesc + " " + classMappedName + "/" + mappingsField.mappedName + " " + mappingsField.mappedDesc);
				srgFields.add("FD: " + classObfName + "/" + mappingsField.obfName() + " " + classMappedName + "/" + mappingsField.mappedName());
			}
			for (var mappingsMethod : mappingsClass.methods) {
				srgMethods.add("MD: " + classObfName + "/" + mappingsMethod.obfName() + " " + mappingsMethod.obfDesc() + " " + classMappedName + "/" + mappingsMethod.mappedName() + " " + mappingsMethod.mappedDesc());
			}
		}
		print(out, srgClasses);
		print(out, srgFields);
		print(out, srgMethods);
	}

	default void toTSRG(final PrintStream out) {
		var classes = getClasses();
		for (var mappingsClass : classes) {
			out.println(mappingsClass.obfName + " " + mappingsClass.mappedName);
			for (var mappingsField : mappingsClass.fields)
				out.println('\t' + mappingsField.obfName() + " " + mappingsField.mappedName());
			for (var mappingsMethod : mappingsClass.methods)
				out.println('\t' + mappingsMethod.obfName() + " " + mappingsMethod.obfDesc() + " " + mappingsMethod.mappedName());
		}
	}

	Mapper parse(InputStream source);

	enum Printer {
		SRG(".srg", Mapper::toSRG),
		TSRG(".tsrg", Mapper::toTSRG),
		;

		private final String extension;
		private final BiConsumer<Mapper, PrintStream> consumer;

		Printer(final String extension, final BiConsumer<Mapper, PrintStream> consumer) {
			this.extension = extension;
			this.consumer = consumer;
		}
	}

}
