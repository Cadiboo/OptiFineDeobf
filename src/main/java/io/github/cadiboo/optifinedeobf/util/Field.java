package io.github.cadiboo.optifinedeobf.util;

/**
 * @author Cadiboo
 */
public class Field {

	public final String owner;
	public final String name;

	public Field(final String owner, final String name) {
		this.owner = owner;
		this.name = name;
	}

	@Override
	public String toString() {
		return owner + "." + name;
	}

}
