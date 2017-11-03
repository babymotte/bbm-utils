package net.bbmsoft.bbm.utils.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Helper class to parallelize the execution of several tasks. This does a
 * similar thing to {@link Stream#forEach(Consumer)} on a parallel Stream but
 * offers more flexibility. E.g. a parallel stream always uses an internal
 * {@link ForkJoinPool} with a target parallelism that corresponds to the number
 * of CPU cores which might not always give the best performance for a large
 * number of tasks with a high latency but low CPU load. Also this Executor
 * allows to change its threads' priority.
 *
 * @author Michael Bachmann
 *
 */
public class ParallelSynchronousExecutor extends ParallelExecutorBase implements ObservableParallelExecutor {

	private final Executor threadPool;
	
	private volatile ProgressMonitor progressmonitor;

	private ParallelSynchronousExecutor(Executor backEnd) {
		this.threadPool = backEnd;
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with an unlimited number of
	 * threads.
	 */
	public static ParallelSynchronousExecutor withCachedThreadPool() {
		return new ParallelSynchronousExecutor(Executors.newCachedThreadPool());
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with an unlimited number of
	 * threads using the specified {@link ThreadFactory}.
	 */
	public static ParallelSynchronousExecutor withCachedThreadPool(ThreadFactory threadFactory) {
		return new ParallelSynchronousExecutor(Executors.newCachedThreadPool(threadFactory));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with an unlimited number of
	 * threads.
	 */
	public static ParallelSynchronousExecutor withCachedThreadPool(String name, boolean daemon) {
		return new ParallelSynchronousExecutor(Executors.newCachedThreadPool(new CustomThreadFactory(name, daemon)));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with an unlimited number of
	 * threads.
	 */
	public static ParallelSynchronousExecutor withCachedThreadPool(String name, boolean daemon, int priority) {
		return new ParallelSynchronousExecutor(
				Executors.newCachedThreadPool(new CustomThreadFactory(name, daemon, priority)));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with the specified maximum
	 * number of threads.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(int nThreads) {
		return new ParallelSynchronousExecutor(Executors.newFixedThreadPool(nThreads));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with the specified maximum
	 * number of threads using the specified {@link ThreadFactory}.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
		return new ParallelSynchronousExecutor(Executors.newFixedThreadPool(nThreads, threadFactory));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with the specified maximum
	 * number of threads.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(int nThreads, String name, boolean daemon) {
		return new ParallelSynchronousExecutor(
				Executors.newFixedThreadPool(nThreads, new CustomThreadFactory(name, daemon)));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with the specified maximum
	 * number of threads.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(int nThreads, String name, boolean daemon,
			int priority) {
		return new ParallelSynchronousExecutor(
				Executors.newFixedThreadPool(nThreads, new CustomThreadFactory(name, daemon, priority)));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with as many threads as the
	 * system has processor cores.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool() {
		return new ParallelSynchronousExecutor(
				Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with as many threads as the
	 * system has processor cores using the specified {@link ThreadFactory}.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(ThreadFactory threadFactory) {
		return new ParallelSynchronousExecutor(
				Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with as many threads as the
	 * system has processor cores.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(String name, boolean daemon) {
		return new ParallelSynchronousExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new CustomThreadFactory(name, daemon)));
	}

	/**
	 * Creates a {@link ParallelSynchronousExecutor} with as many threads as the
	 * system has processor cores.
	 */
	public static ParallelSynchronousExecutor withFixedThreadPool(String name, boolean daemon, int priority) {
		return new ParallelSynchronousExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new CustomThreadFactory(name, daemon, priority)));
	}

	/**
	 * Executes all given {@link Runnable Runnables} in parallel. The method
	 * will return as soon as all runnables have either completed or thrown an
	 * exception.
	 *
	 * @param actions
	 *            all the {@link Runnable Runnables} to be executed
	 */
	@Override
	public void execute(Collection<? extends Runnable> actions) {

		CountDownLatch cdl = new CountDownLatch(actions.size());
		ProgressMonitor monitor = this.progressmonitor;
		int total = actions.size();
		AtomicInteger counter = monitor != null ? new AtomicInteger() : null;
		
		if(monitor != null) {
			monitor.updateProgress(0, total);
		}

		for (Runnable a : actions) {
			this.threadPool.execute(() -> {
				try {
					a.run();
				} finally {
					if(monitor != null) {
						monitor.updateProgress(counter.incrementAndGet(), total);
					}
					cdl.countDown();
				}
			});
		}

		try {
			cdl.await();
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Executes all given {@link Runnable Runnables} in parallel. The method
	 * will return as soon as all runnables have either completed or thrown an
	 * exception.
	 *
	 * @param actions
	 *            all the {@link Runnable Runnables} to be executed
	 */
	@Override
	public void execute(Iterator<? extends Runnable> actions) {

		List<Runnable> runnables = new ArrayList<>();

		while (actions.hasNext()) {
			runnables.add(actions.next());
		}

		execute(runnables);
	}

	/**
	 * Perform the specified {@link Consumer} on every element of the specified
	 * {@link Iterable} in parallel. The method will return as soon as all
	 * consumer operations have successfully completed or thrown an exception.
	 */
	// optimization: avoid unnecessarily copying into a new collection
	@Override
	public <T> void forEachParallel(Iterable<T> items, Consumer<? super T> consumer) {

		if (!(items instanceof Collection)) {
			forEachParallel(items.iterator(), consumer);
			return;
		}

		int size = ((Collection<T>) items).size();

		CountDownLatch cdl = new CountDownLatch(size);

		items.forEach(i -> this.threadPool.execute(() -> {
			try {
				consumer.accept(i);
			} finally {
				cdl.countDown();
			}
		}));

		try {
			cdl.await();
		} catch (InterruptedException e) {
			return;
		}

	}

	/**
	 * Perform the specified {@link Consumer} on every element of the specified
	 * {@link Iterable} in parallel. The method will return as soon as all
	 * consumer operations have successfully completed or thrown an exception.
	 */
	// optimization: avoid unnecessarily copying into a new collection
	@Override
	public <T> void forEachParallel(Iterable<T> items, BiConsumer<? super T, Integer> consumer) {

		if (!(items instanceof Collection)) {
			forEachParallel(items.iterator(), consumer);
			return;
		}

		int size = ((Collection<T>) items).size();

		CountDownLatch cdl = new CountDownLatch(size);
		
		int counter = 0;
		for(T item : items) {
			final int i = counter++;
			this.threadPool.execute(() -> {
				try {
					consumer.accept(item, i);
				} finally {
					cdl.countDown();
				}
			});
		}

		try {
			cdl.await();
		} catch (InterruptedException e) {
			return;
		}

	}

	@Override
	public void setProgressMonitor(ProgressMonitor monitor) {
		this.progressmonitor = monitor;
	}
}
