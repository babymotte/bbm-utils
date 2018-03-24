package net.bbmsoft.bbm.utils;

/**
 * Simple interface defining the capability to lock and unlock something.
 * <p>
 * This interface does not impose any requirements about what being locked or
 * unlocked implies for the implementing object.
 * 
 * @author Michael Bachmann
 *
 */
public interface Lockable {

	/**
	 * Locks this {@link Lockable}.
	 */
	public abstract void lock();

	/**
	 * Unocks this {@link Lockable}.
	 */
	public abstract void unlock();

}
