package net.bbmsoft.bbm.utils.subapplication;

public interface UnsafeInstanceSupplier<T> {
	
	public T getInstance() throws InstantiationException, IllegalAccessException;
}
