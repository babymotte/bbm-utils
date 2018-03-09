package net.bbmsoft.bbm.utils.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ConsumingFuture<T> implements Future<T>, Consumer<T> {

	private final Object lock;

	private final Runnable onCompletion;
	private final Runnable onCancel;
	private final Runnable onTimeout;

	private boolean cancelled;
	private boolean done;

	private T ref;

	private final long start = System.currentTimeMillis();

	public ConsumingFuture() {
		this(null);
	}

	public ConsumingFuture(Runnable onCompletion) {
		this.onCompletion = onCompletion;
		this.lock = new Object();
		this.onCancel = null;
		this.onTimeout = null;
	}

	public ConsumingFuture(Runnable onCompletion, Runnable onCancel) {
		this.onCompletion = onCompletion;
		this.lock = new Object();
		this.onCancel = onCancel;
		this.onTimeout = null;
	}

	public ConsumingFuture(Runnable onCompletion, Runnable onCancel, Runnable timeout) {
		this.onCompletion = onCompletion;
		this.lock = new Object();
		this.onCancel = onCancel;
		this.onTimeout = timeout;
	}

	@Override
	public void accept(T t) {

		synchronized (this.lock) {

			if (this.done || this.cancelled) {
				return;
			}

			this.ref = t;
			this.done = true;

			if (this.onCompletion != null) {
				this.onCompletion.run();
			}

			this.lock.notifyAll();
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {

		synchronized (this.lock) {

			// check preconditions

			// cannot cancel, if it has already been cancelled
			if (this.cancelled) {
				return false;
			}

			// cannot cancel, if it is already done
			if (this.done) {
				return false;
			}

			this.cancelled = true;

			if (this.onCancel != null) {
				this.onCancel.run();
			}

			if (this.onCompletion != null) {
				this.onCompletion.run();
			}

			this.lock.notifyAll();

			return true;
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (this.lock) {
			return this.cancelled;
		}
	}

	@Override
	public boolean isDone() {
		synchronized (this.lock) {
			return this.done;
		}
	}

	@Override
	public T get() throws InterruptedException {

		synchronized (this.lock) {
			while (!this.done && !this.cancelled) {
				this.lock.wait();
			}
		}

		// at this point, either accept() or cancel() has been called, so
		// onCompletion has already been executed

		return this.ref;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {

		long timeoutMillis = unit.toMillis(timeout);
		long end = this.start + timeoutMillis;

		synchronized (this.lock) {
			while (!this.done && !this.cancelled && System.currentTimeMillis() < end) {
				this.lock.wait((long) (timeoutMillis * 1.1));
			}
		}

		T result = this.ref;

		if (!this.done && this.onTimeout != null) {
			this.onTimeout.run();
		}

		// at this point, onCompletion has only been executed if either accept()
		// or cancel() has been called, but not if a timeout occurred

		if (!this.done && !this.cancelled && this.onCompletion != null) {
			this.onCompletion.run();
		}

		if (this.done || this.cancelled) {
			return result;
		} else {
			throw new TimeoutException("Value could not be retrieved within the specified timeout.");
		}

	}

	public Runnable getOnCompletion() {
		return onCompletion;
	}

	public Runnable getOnCancel() {
		return onCancel;
	}

	public Runnable getOnTimeout() {
		return onTimeout;
	}

}
