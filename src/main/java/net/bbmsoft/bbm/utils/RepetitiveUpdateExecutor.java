package net.bbmsoft.bbm.utils;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A typically single threaded executor that is optimized to run repetitive self
 * contained tasks. In contrast to other executors, this one does not use a
 * queue to manage pending tasks but maintains only one single pending task.
 * That means if a new task comes in when another one is still pending, the
 * pending task will be removed and never be executed.
 * <p>
 * This is useful when the same task (like e.g. updating a single UI element) is
 * performed over and over again (e.g. in response to a certain event) while
 * actually only the result of the last task is really relevant.
 * <p>
 * Additionally, when executing a command a delay can be specified, to make sure
 * only a certain amount of tasks per second is actually executed, independent
 * of how long it takes to complete one task.
 * <p>
 * This executor can either create its own worker thread or use another executor
 * to which tasks will be delegated. This is helpful when managing tasks that
 * have to be run on a special thread, like for example when updating UI
 * elements in a JavaFX application. To create a RepetitiveUpdateExecutor for
 * updating a certain UI element you could for example use
 * {@code new RepetitiveUpdateExecutor(Platform::runLater)}
 * <p>
 * Notice that every invocation of {@link #execute(Runnable)} or
 * {@link #execute(Runnable, long)} will cancel ANY pending task on the same
 * executor object. Thus it is very important to use separate
 * RepetitiveUpdateExecutor instances for different jobs. It is however possible
 * to use the same worker thread for different RepetitiveUpdateExecutor
 * instances.
 *
 * @author Michael Bachmann
 *
 */
public class RepetitiveUpdateExecutor implements Executor {

	private final AtomicReference<Runnable> task;
	private final Executor worker;

	/**
	 * Creates a new RepetitiveUpdateExecutor that uses its own worker thread in
	 * which tasks will be executed.
	 */
	public RepetitiveUpdateExecutor() {
		this(Executors.newSingleThreadExecutor());
	}

	/**
	 * Creates a new RepetitiveUpdateExecutor that delegates tasks to the given
	 * {@link Executor}. Notice that the same delegate Executor can be used for
	 * any number of RepetitiveUpdateExecutors and also to execute any other
	 * task independently of the created RepetitiveUpdateExecutor object.
	 *
	 * @param worker
	 *            tasks to be executed will eventually be delegated to this
	 *            executor
	 */
	public RepetitiveUpdateExecutor(Executor worker) {
		this.task = new AtomicReference<>();
		this.worker = worker;
	}

	/**
	 * Register a command for execution. This will cancel ANY commands still
	 * pending to be executed by this RepetitiveUpdateExecutor. Likewise, this
	 * command will be cancelled if any other command is registered for
	 * execution before execution of this command has started.
	 * <p>
	 * Otherwise this command will be executed as soon as this
	 * RepetitiveUpdateExecutor's worker thread is free.
	 *
	 * @param command
	 *            the Runnable to be registered for execution
	 * @throws NullPointerException
	 *             if the given command is {@code null}, a
	 *             {@code NullPointerException} will be thrown
	 */
	@Override
	public void execute(Runnable command) {
		execute(Objects.requireNonNull(command), 0);
	}

	/**
	 * This is an alternative to the normal {@link #execute(Runnable)} function
	 * that can be used if {@code command} itself is an asynchronous call that
	 * returns immediately, however the function it asynchronously calls takes a
	 * certain time to complete. The delay simulates a longer duration for the
	 * call to complete so that the actual command has enough time, before it
	 * can be called again.
	 * <p>
	 * Notice that this functions blocks this executor's worker thread for at
	 * least the duration of the delay, so it is not recommended to use this
	 * function, if the worker thread is also used for tasks not related to this
	 * executor (e.g. if the executor uses the JavaFX Application Thread as
	 * worker thread in a UI application).
	 *
	 * @param command
	 *            the Runnable to be registered for execution
	 * @param delay
	 *            the number of milliseconds the next command will be delayed,
	 *            after this one has completed
	 *
	 * @throws NullPointerException
	 *             if the given command is {@code null}, a
	 *             {@code NullPointerException} will be thrown
	 */
	public void execute(Runnable command, long delay) {

		if (task.getAndSet(Objects.requireNonNull(command)) == null) {
			worker.execute(() -> {
				task.getAndSet(null).run();
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
