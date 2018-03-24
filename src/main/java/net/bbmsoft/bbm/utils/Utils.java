package net.bbmsoft.bbm.utils;

import java.util.function.Function;

/**
 * Collection of static utility classes that are too small to get their own
 * class.
 * 
 * @author Michael Bachmann
 *
 */
public class Utils {

	/**
	 * Get the root of an object hierarchy.<br>
	 * Example:
	 * 
	 * <pre>
	 * Throwable rootCause = Utils.getRoot(exception, e -> e.getCause());
	 * </pre>
	 * 
	 * @param object
	 *            any object in some object hierarchy
	 * @param getParent
	 *            the function to retrieve an object's parent
	 * 
	 * @return the root of the hierarchy, i.e. the first object that does not have a
	 *         parent
	 */
	@SuppressWarnings("unchecked")
	public static <T, V extends T> T getRoot(V object, Function<V, T> getParent) {

		V current;
		T parent;

		current = object;

		while ((parent = getParent.apply(current)) != null) {
			current = (V) parent;
		}

		return current;
	}
}
