package io.github.cadiboo.optifinedeobf.mapping;

/**
 * @author Cadiboo
 */
public class Obf2MCP implements MappingService {

	private final Obf2SRG obf2SRG = new Obf2SRG();
	private final SRG2MCP srg2MCP = new SRG2MCP();

	@Override
	public void dump() {
	}

	@Override
	public String mapClass(final String name) {
		return srg2MCP.mapClass(obf2SRG.mapClass(name));
	}

	@Override
	public String mapField(final String className, final String name) {
		return srg2MCP.mapField(obf2SRG.mapClass(className), obf2SRG.mapField(className, name));
	}

	@Override
	public String mapMethod(final String className, final String name) {
		return srg2MCP.mapMethod(obf2SRG.mapClass(className), obf2SRG.mapMethod(className, name));
	}

}
