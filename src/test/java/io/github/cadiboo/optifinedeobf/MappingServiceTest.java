package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;

/**
 * @author Cadiboo
 */
public class MappingServiceTest {

	public static void main(String[] args) {
		final MappingService[] mappingServices = {
				new SRG2MCP(),
				new TSRG2MCP(),
		};
		for (MappingService service : mappingServices) {
			System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
			service.dump();
		}
		System.out.println();
		System.out.println();

		final Field[] fields = {
				new Field("net/minecraft/client/renderer/Quaternion", "field_195895_a"),
				new Field("net/minecraft/client/renderer/Vector3f", "field_195907_a"),
		};
		final Method[] methods = {
				new Method("net/minecraft/client/renderer/Vector3f", "func_195900_b", "()F"),
				new Method("net/minecraft/client/renderer/Vector3f", "func_195906_d", "()V"),
		};
		for (MappingService service : mappingServices) {
			System.out.println("~~~" + service.getClass().getSimpleName() + "~~~");
			for (final Field field : fields)
				System.out.println("Field: " + field + " -> " + field.owner + "." + service.mapField(field.owner, field.name));
			for (final Method method : methods)
				System.out.println("Method: " + method + " -> " + method.owner + "." + service.mapMethod(method.owner, method.name, method.desc));
		}

	}

}
