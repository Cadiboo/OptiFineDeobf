package io.github.cadiboo.optifinedeobf;

/**
 * @author Cadiboo
 */
public class Method {

	final String owner;
	final String name;
	final String desc;

	Method(final String owner, final String name, final String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	@Override
	public String toString() {
		return owner + "." + name + desc;
	}

}
