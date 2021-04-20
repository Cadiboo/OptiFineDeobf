package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Cadiboo
 */
public class MappingServiceTest {

	// These are just small compacted samples from 'srg_to_snapshot_20190922-1.14.3'
	private static final String TEST_SRG = "" +
		"CL: net/minecraft/client/renderer/Quaternion net/minecraft/client/renderer/Quaternion\n" +
		"CL: net/minecraft/client/renderer/Vector3f net/minecraft/client/renderer/Vector3f\n" +
		"FD: net/minecraft/client/renderer/Quaternion/field_195895_a net/minecraft/client/renderer/Quaternion/components\n" +
		"FD: net/minecraft/client/renderer/Vector3f/field_195907_a net/minecraft/client/renderer/Vector3f/components\n" +
		"MD: net/minecraft/client/renderer/Vector3f/func_195906_d ()V net/minecraft/client/renderer/Vector3f/normalize ()V\n" +
		"MD: net/minecraft/client/renderer/Vector3f/func_195900_b ()F net/minecraft/client/renderer/Vector3f/getY ()F\n" +
		"";
	private static final String TEST_TSRG = "" +
		"net/minecraft/client/renderer/Quaternion net/minecraft/client/renderer/Quaternion\n" +
		"\tfield_195895_a components\n" +
		"\tequals (Ljava/lang/Object;)Z equals\n" +
		"\tfunc_195891_b ()F getY\n" +
		"\tfunc_195892_e ()V conjugate\n" +
		"net/minecraft/client/renderer/Vector3f net/minecraft/client/renderer/Vector3f\n" +
		"\tfield_195907_a components\n" +
		"\tequals (Ljava/lang/Object;)Z equals\n" +
		"\tfunc_195896_c (Lnet/minecraft/client/renderer/Vector3f;)V cross\n" +
		"\tfunc_195900_b ()F getY\n" +
		"\tfunc_214906_c (FFF)F func_214906_c\n" +
		"\thashCode ()I hashCode\n" +
		"";

	public static void main(String[] args) {
		final MappingService[] mappingServices = {
				new SRG2MCP(new ByteArrayInputStream(TEST_SRG.getBytes())),
				new TSRG2MCP(new ByteArrayInputStream(TEST_TSRG.getBytes())),
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
