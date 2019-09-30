package io.github.cadiboo.optifinedeobf.util;

/**
 * @author Cadiboo
 */
public class Method {

	public final String owner;
	public final String name;
	public final String desc;

	public Method(final String owner, final String name, final String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	@Override
	public String toString() {
		return owner + "." + name + desc;
	}

}
