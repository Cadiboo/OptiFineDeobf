package io.github.cadiboo.optifinedeobf.mapper;

import io.github.cadiboo.optifinedeobf.mapper.util.MappingsClass;
import io.github.cadiboo.optifinedeobf.mapper.util.MappingsField;
import io.github.cadiboo.optifinedeobf.mapper.util.MappingsMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Cadiboo
 */
public interface Mapper {

	static void main(String... args) throws IOException {
		convert("/Users/Cadiboo/Desktop/client.txt", Printer.TSRG);
		convert("/Users/Cadiboo/Desktop/client.txt", Printer.SRG);
		convert("/Users/Cadiboo/Desktop/obf2srg.tsrg", Printer.SRG);
		convert("/Users/Cadiboo/Desktop/srg_to_snapshot_20190922-1.14.3.srg", Printer.TSRG);
//		new Proguard().parse(new FileInputStream("/Users/Cadiboo/Desktop/client.txt")).toSRG(System.out);
	}

	static void convert(String name, Printer printer) throws IOException {
		final File file = new File(name);
		final String fileName = file.getName();

		final Mapper mapper;
		if (fileName.endsWith(".txt")) mapper = new Proguard();
		else if (fileName.endsWith(".srg")) mapper = new SRG();
		else if (fileName.endsWith(".tsrg")) mapper = new TSRG();
		else throw new IllegalStateException("Unsupported mappings extension");

		try (FileInputStream source = new FileInputStream(file)) {
			mapper.parse(source);
		}
		try (FileOutputStream out = new FileOutputStream(Paths.get(file.toPath().toString() + printer.extension).toFile())) {
			try (PrintStream printStream = new PrintStream(out)) {
				printer.consumer.accept(mapper, printStream);
			}
		}

	}

	static void print(final PrintStream out, ArrayList<String> strings) {
		for (final String s : strings) out.println(s);
	}

	List<MappingsClass> getClasses();

	default void toSRG(PrintStream out) {
		final List<MappingsClass> classes = getClasses();

		final ArrayList<String> srgClasses = new ArrayList<>();
		final ArrayList<String> srgFields = new ArrayList<>();
		final ArrayList<String> srgMethods = new ArrayList<>();

		for (final MappingsClass mappingsClass : classes) {

			final String classObfName = mappingsClass.obfName;
			final String classMappedName = mappingsClass.mappedName;

			srgClasses.add("CL: " + classObfName + " " + classMappedName);

			for (final MappingsField mappingsField : mappingsClass.fields) {
//				srgFields.add("FD: " + classObfName + "/" + mappingsField.obfName + " " + mappingsField.obfDesc + " " + classMappedName + "/" + mappingsField.mappedName + " " + mappingsField.mappedDesc);
				srgFields.add("FD: " + classObfName + "/" + mappingsField.obfName + " " + classMappedName + "/" + mappingsField.mappedName);
			}
			for (final MappingsMethod mappingsMethod : mappingsClass.methods) {
				srgMethods.add("MD: " + classObfName + "/" + mappingsMethod.obfName + " " + mappingsMethod.obfDesc + " " + classMappedName + "/" + mappingsMethod.mappedName + " " + mappingsMethod.mappedDesc);
			}
		}
		print(out, srgClasses);
		print(out, srgFields);
		print(out, srgMethods);
	}

	default void toTSRG(final PrintStream out) {
		final List<MappingsClass> classes = getClasses();
		for (final MappingsClass mappingsClass : classes) {
			out.println(mappingsClass.obfName + " " + mappingsClass.mappedName);
			for (final MappingsField mappingsField : mappingsClass.fields)
				out.println('\t' + mappingsField.obfName + " " + mappingsField.mappedName);
			for (final MappingsMethod mappingsMethod : mappingsClass.methods)
				out.println('\t' + mappingsMethod.obfName + " " + mappingsMethod.obfDesc + " " + mappingsMethod.mappedName);
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
