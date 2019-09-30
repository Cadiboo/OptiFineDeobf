package io.github.cadiboo.optifinedeobf.mapping;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public enum MappingServiceType {

	SRG2MCP("SRG2MCP", "", SRG2MCP::new),
	TSRG2MCP("TSRG2MCP", "", TSRG2MCP::new),
	Obf2SRG("Obf2SRG", "", Obf2SRG::new),
	Obf2MCP("Obf2MCP", "", Obf2MCP::new),

	;

	private final String name;
	private final String description;
	private final Supplier<MappingService> supplier;
	private MappingService mappingService;

	MappingServiceType(final String name, final String description, final Supplier<MappingService> supplier) {

		this.name = name;
		this.description = description;
		this.supplier = supplier;
	}

	public MappingService getMappingService() {
		if (mappingService == null)
			mappingService = supplier.get();
		return mappingService;
	}
}
