package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.Obf2MCP;
import io.github.cadiboo.optifinedeobf.mapping.Obf2SRG;
import io.github.cadiboo.optifinedeobf.util.Field;
import io.github.cadiboo.optifinedeobf.util.Method;

/**
 * @author Cadiboo
 */
public class MappingServiceTest {

	public static void main(String[] args) {
		final MappingService[] mappingServices = {
//				new SRG2MCP(),
//				new TSRG2MCP(),
				new Obf2SRG(),
				new Obf2MCP(),
		};
		for (MappingService service : mappingServices) {
			System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
			service.dump();
		}
		System.out.println();
		System.out.println();

		final String[] classes = {
				"a",
				"b",
				"cum",
		};
		final Field[] fields = {
				new Field("a", "a"),
				new Field("cuz", "a"),
				new Field("net/minecraft/client/renderer/Quaternion", "field_195895_a"),
				new Field("net/minecraft/client/renderer/Vector3f", "field_195907_a"),
		};
		final Method[] methods = {
				new Method("a", "a", "()F"),
				new Method("a", "a", "(F)F"),
				new Method("a", "a", "(La;)V"),
				new Method("a", "b", "()F"),
				new Method("a", "b", "(F)F"),
				new Method("cuz", "a", "()Lcuz;"),
				new Method("net/minecraft/client/renderer/Vector3f", "func_195900_b", "()F"),
				new Method("net/minecraft/client/renderer/Vector3f", "func_195906_d", "()V"),
		};
		for (MappingService service : mappingServices) {
			System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
			for (final String clazz : classes)
				System.out.println("Class: " + clazz + " -> " + service.mapClass(clazz));
			for (final Field field : fields)
				System.out.println("Field: " + field + " -> " + service.mapClass(field.owner) + "." + service.mapField(field.owner, field.name));
			for (final Method method : methods)
				System.out.println("Method: " + method + " -> " + service.mapClass(method.owner) + "." + service.mapMethod(method.owner, method.name, method.desc) + service.mapMethodDescriptor(method.desc));
		}

	}

}
