package net.bbmsoft.bbm.utils;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 
 * This interface defines the general capability to persist and load objects of
 * a certain type. It does not impose any requirements about how these objects
 * are being persisted.
 * 
 * @author Michael Bachmann
 *
 * @param <T>
 *            the generic type declaration of the objects a persistor instance
 *            can persist and load
 */
public interface Persistor<T> {

	/**
	 * Persists the given object. The general contract here is that when
	 * {@link #load(Consumer)} or {@link #load(Consumer, ExceptionHandler)} is
	 * called at a later time, the specified consumer will be provided with an
	 * object that is equal* to the object that has last been persisted.
	 * <p>
	 * Any exceptions happening during the process should be passed to the specified
	 * {@link ExceptionHandler}, which needs to be able to handle Exceptions of any
	 * type.
	 * <p>
	 * *as in {@link Objects#equals(Object, Object) Objects.equals(persistedObject,
	 * loadedObject)} == true
	 * 
	 * @param object
	 *            the object to be persisted
	 * @param exceptionHandler
	 *            handler for any exceptions that happen while persisting
	 * @see #load(Consumer)
	 * @see #load(Consumer, ExceptionHandler)
	 */
	public void persist(T object, ExceptionHandler exceptionHandler);

	/**
	 * Loads the object that has last been persisted by calling the
	 * {@link #persist(Object)} or {@link #persist(Object, ExceptionHandler)}
	 * method.
	 * 
	 * @param loadCallback
	 *            should be called with the loaded object once loading is complete.
	 *            Whether or not loading happens on a background thread is up to the
	 *            implementer
	 * @param exceptionHandler
	 *            handler for any exceptions that happen while persisting
	 * 
	 * @see #persist(Object)
	 * @see #persist(Object, ExceptionHandler)
	 */
	public void load(Consumer<T> loadCallback, ExceptionHandler exceptionHandler);

	/**
	 * Persists the given object as described for
	 * {@link #persist(Object, ExceptionHandler)}. This method makes exception
	 * handling optional by just rethrowing any (including checked) exceptions. This
	 * should only be used when the persistence layer used does not throw any
	 * exceptions or when recovering from a persistence exception is not feasible
	 * anyway (e.g. while testing).
	 * 
	 * @param object
	 *            the object to be persisted
	 * @see #persist(Object, ExceptionHandler)
	 */
	public default void persist(T object) {
		this.persist(object, new SneakilyThrowingExceptionHandler());
	}

	/**
	 * Loads the object that has last been persisted as described for
	 * {@link #load(Consumer, ExceptionHandler)}. This method makes exception
	 * handling optional by just rethrowing any (including checked) exceptions. This
	 * should only be used when the persistence layer used does not throw any
	 * exceptions or when recovering from a persistence exception is not feasible
	 * anyway (e.g. while testing).
	 * 
	 * @param loadCallback
	 *            should be called with the loaded object once loading is complete.
	 *            Whether or not loading happens on a background thread is up to the
	 *            implementer
	 * 
	 * @see #load(Consumer, ExceptionHandler)
	 */
	public default void load(Consumer<T> loadCallback) {
		this.load(loadCallback, new SneakilyThrowingExceptionHandler());
	}

	/**
	 * Interface defining the capability of handling exceptions that happen during
	 * loading or persisting an object.
	 * 
	 * @author Michael Bachmann
	 *
	 */
	public static interface ExceptionHandler {

		/**
		 * Do whatever needs to be done when an exception happens during loading or
		 * persisting.
		 * 
		 * @param e
		 *            the exception to be handled
		 */
		public void handle(Throwable e);
	}

	/**
	 * Simple {@link ExceptionHandler} implementation that just rethrows any
	 * (including checked) exceptions. This should only be used when the persistence
	 * layer used does not throw any exceptions or when recovering from a
	 * persistence exception is not feasible anyway (e.g. while testing).
	 * 
	 * @author Michael Bachmann
	 *
	 */
	public static class SneakilyThrowingExceptionHandler implements ExceptionHandler {

		/**
		 * Rethrows the exception instead of handling it.
		 */
		@Override
		public void handle(Throwable e) {
			sneakyThrow(e);
		}

		@SuppressWarnings("unchecked")
		private <TH extends Throwable> void sneakyThrow(Throwable e) throws TH {
			throw (TH) e;
		}
	}

}
