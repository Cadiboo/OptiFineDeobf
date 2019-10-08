package io.github.cadiboo.optifinedeobf;

/**
 * @author Cadiboo
 */
public class Field {

	final String owner;
	final String name;

	Field(final String owner, final String name) {
		this.owner = owner;
		this.name = name;
	}

	@Override
	public String toString() {
		return owner + "." + name;
	}

}
