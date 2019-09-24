package io.github.cadiboo.optifinedeobf.mapping;

/**
 * @author Cadiboo
 */
public interface MappingService {

	static void main(String[] args) {
		final MappingService[] mappingServices = {
				new SRG2MCP(),
				new TSRG2MCP(),
				new Obf2SRG(),
				new Obf2MCP(),
//				new Obf2MCP()
		};
		for (MappingService service : mappingServices) {
			System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
			service.dump();
		}
		args = new String[]{
				"a", "a.b",
				"cum", "cum.a", "cum.b", "cum.c",
		};
		for (final String arg : args) {
			System.out.println("Mapping \"" + arg + "\"");
			for (MappingService service : mappingServices) {
				System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
				System.out.println(service.mapClass(arg));
				String[] s = arg.split("\\.");
				if (s.length > 1) {
					System.out.println(service.mapField(s[0], s[1]));
					System.out.println(service.mapMethod(s[0], s[1]));
				}
			}
			System.out.println();
		}
	}

	void dump();

	String mapClass(String name);

	String mapField(String className, String name);

	String mapMethod(String className, String name);

}
