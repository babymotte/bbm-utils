package net.bbmsoft.bbm.utils;

import java.util.function.Function;

public class Utils {

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
