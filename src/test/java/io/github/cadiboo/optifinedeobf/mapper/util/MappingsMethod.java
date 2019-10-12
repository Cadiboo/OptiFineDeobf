package io.github.cadiboo.optifinedeobf.mapper.util;

/**
 * @author Cadiboo
 */
public
class MappingsMethod {

	public String obfName;
	public String mappedName;
	public String obfDesc;
	public String mappedDesc;

	public MappingsMethod(final String obfName, final String mappedName, final String obfDesc, final String mappedDesc) {
		this.obfName = obfName;
		this.mappedName = mappedName;
		this.obfDesc = obfDesc;
		this.mappedDesc = mappedDesc;
	}

}
