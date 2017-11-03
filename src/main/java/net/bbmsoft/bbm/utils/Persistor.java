package net.bbmsoft.bbm.utils;

import java.util.function.Consumer;

public interface Persistor<T> {

	public void persist(T object, ExceptionHandler exceptionHandler);

	public void load(Consumer<T> loadCallback, ExceptionHandler exceptionHandler);

	public default void persist(T object) {
		this.persist(object, new SneakilyThrowingExceptionHandler());
	}

	public default void load(Consumer<T> loadCallback) {
		this.load(loadCallback, new SneakilyThrowingExceptionHandler());
	}

	public static interface ExceptionHandler {
		public void handle(Throwable e);
	}

	public static class SneakilyThrowingExceptionHandler implements ExceptionHandler {

		@Override
		public  void handle(Throwable e) {
			sneakyThrow(e);
		}

		@SuppressWarnings("unchecked")
		private <TH extends Throwable> void sneakyThrow(Throwable e) throws TH {
		    throw (TH) e;
		}
	}

}
