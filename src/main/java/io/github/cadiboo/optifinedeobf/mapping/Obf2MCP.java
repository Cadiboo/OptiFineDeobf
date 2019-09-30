package io.github.cadiboo.optifinedeobf.mapping;

/**
 * @author Cadiboo
 */
public class Obf2MCP implements MappingService {

	private final Obf2SRG obf2SRG = new Obf2SRG();
	private final SRG2MCP srg2MCP = new SRG2MCP();

	@Override
	public void dump() {
		obf2SRG.dump();
		srg2MCP.dump();
	}

	@Override
	public String mapClass(final String clazz) {
		return srg2MCP.mapClass(obf2SRG.mapClass(clazz));
	}

	@Override
	public String mapField(final String clazz, final String name) {
		return srg2MCP.mapField(obf2SRG.mapClass(clazz), obf2SRG.mapField(clazz, name));
	}

	@Override
	public String mapMethod(String clazz, String name, String desc) {
		return srg2MCP.mapMethod(obf2SRG.mapClass(clazz), obf2SRG.mapMethod(clazz, name, desc), mapMethodDescriptor(desc));
	}

	@Override
	public String mapTypeClass(final String clazz) {
		return obf2SRG.mapClass(clazz);
	}

	@Override
	public boolean needsClassNameRemapping() {
		return true;
	}

}
