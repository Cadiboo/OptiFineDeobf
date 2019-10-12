package io.github.cadiboo.optifinedeobf.mapper.util;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public
class MappingsClass {

	public String obfName;
	public String mappedName;
	public ArrayList<MappingsField> fields = new ArrayList<>();
	public ArrayList<MappingsMethod> methods = new ArrayList<>();

	public MappingsClass(final String obfName, final String mappedName) {
		this.obfName = obfName;
		this.mappedName = mappedName;
	}

}
