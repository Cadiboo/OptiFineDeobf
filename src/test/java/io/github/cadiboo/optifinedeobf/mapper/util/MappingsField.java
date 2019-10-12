package io.github.cadiboo.optifinedeobf.mapper.util;

/**
 * @author Cadiboo
 */
public
class MappingsField {

	public String obfName;
	public String mappedName;
	public String obfDesc;
	public String mappedDesc;

	public MappingsField(final String obfName, final String mappedName, final String obfDesc, final String mappedDesc) {
		this.obfName = obfName;
		this.mappedName = mappedName;
		this.obfDesc = obfDesc;
		this.mappedDesc = mappedDesc;
	}

}
