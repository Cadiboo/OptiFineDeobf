package io.github.cadiboo.optifinedeobf.mapping;

/**
 * @author Cadiboo
 */
public interface MappingService {

	void dump();

	/**
	 * @param clazz The unmapped internal name of the field's class
	 * @param name  The unmapped name of the field
	 * @return The mapped name of field
	 */
	String mapField(String clazz, String name);

	/**
	 * @param clazz The unmapped internal name of the method's class
	 * @param name  The unmapped name of the method
	 * @param desc  The unmapped description of the method
	 * @return The mapped name of method
	 */
	String mapMethod(String clazz, String name, String desc);

}
