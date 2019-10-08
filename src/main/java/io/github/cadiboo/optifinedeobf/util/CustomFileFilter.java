package io.github.cadiboo.optifinedeobf.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public class CustomFileFilter extends FileFilter {

	private final String description;
	private final Predicate<File> predicate;

	public CustomFileFilter(final String description, final Predicate<File> predicate) {
		this.description = description;
		this.predicate = predicate;
	}

	@Override
	public boolean accept(final File f) {
		return predicate.test(f);
	}

	@Override
	public String getDescription() {
		return description;
	}

}
