package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import org.junit.Test;
import org.objectweb.asm.Handle;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.H_GETFIELD;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;

/**
 * @author Cadiboo
 */
public class ClassRemapperTest {

	@Test
	public void lambdaMethodsShouldBeMappedProperly() {
		var remapper = new ClassRemapper(new SRG2MCP(new ByteArrayInputStream("""
			MD: foo/Bar/func_1111 ()V foo/Bar/mappedMethod ()V
			FD: foo/Bar/field_1111 foo/Bar/mappedField
			""".getBytes(StandardCharsets.UTF_8))), true, true);

		assertEquals("mappedMethod", remapper.mapLambdaMethod("foo/Bar", "func_1111", "()V"));
		assertEquals("func_1112", remapper.mapLambdaMethod("foo/Bar", "func_1112", "()V"));

		assertEquals("mappedField", remapper.mapHandleName(new Handle(H_GETFIELD, "foo/Bar", "field_1111", "D", false)));
		assertEquals("field_1112", remapper.mapHandleName(new Handle(H_GETFIELD, "foo/Bar", "field_1112", "D", false)));

		assertEquals("lambda$mappedMethod$0", remapper.mapHandleName(new Handle(H_INVOKEVIRTUAL, "", "lambda$func_1111$0", "()V", false)));
		assertEquals("lambda$func_1112$0", remapper.mapHandleName(new Handle(H_INVOKEVIRTUAL, "", "lambda$func_1112$0", "()V", false)));
	}

}
