package net.bbmsoft.bbm.utils.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ParallelExecutor {

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Collection} in parallel if supported by the underlying platform and
	 * the concrete implementation.
	 */
	public default <T> void forEachParallel(Collection<T> iterable, Consumer<? super T> consumer) {
		forEachParallel((Iterable<T>) iterable, consumer);
	}

	public default <K, V> void forEachParallel(Map<K, V> map, BiConsumer<? super K, ? super V> consumer) {
		forEachParallel(map.entrySet(), e -> consumer.accept(e.getKey(), e.getValue()));
	}

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Collection} in parallel if supported by the underlying platform and
	 * the concrete implementation.
	 */
	public default <T> void forEachParallel(Collection<T> iterable, BiConsumer<? super T, Integer> consumer) {
		forEachParallel((Iterable<T>) iterable, consumer);
	}

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Iterable} in parallel if supported by the underlying platform and the
	 * concrete implementation.
	 */
	public default <T> void forEachParallel(Iterable<T> iterable, Consumer<? super T> consumer) {
		forEachParallel(iterable.iterator(), consumer);
	}

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Iterable} in parallel if supported by the underlying platform and the
	 * concrete implementation.
	 */
	public default <T> void forEachParallel(Iterable<T> iterable, BiConsumer<? super T, Integer> consumer) {
		forEachParallel(iterable.iterator(), consumer);
	}

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Iterator} in parallel if supported by the underlying platform and the
	 * concrete implementation.
	 */
	public default <T> void forEachParallel(Iterator<T> iterator, Consumer<? super T> consumer) {
		Iterator<Runnable> iter = new TransformingIterator<>(iterator, i -> (() -> consumer.accept(i)));
		execute(iter);
	}

	/**
	 * Applies the specified {@link Consumer} to all elements of the specified
	 * {@link Iterator} in parallel if supported by the underlying platform and the
	 * concrete implementation.
	 */
	public default <T> void forEachParallel(Iterator<T> iterator, BiConsumer<? super T, Integer> consumer) {
		CountingIterator<T> iter = new CountingIterator<>(iterator);
		TransformingIterator<T, Runnable> transformingIterator = new TransformingIterator<>(iter,
				t -> (() -> consumer.accept(t, iter.getIndex())));
		execute(transformingIterator);
	}

	/**
	 * Executes all elements of the specified array in parallel if supported by the
	 * underlying platform and the concrete implementation.
	 */
	public default void execute(Runnable... tasks) {
		execute(Arrays.asList(tasks));
	}

	/**
	 * Executes all elements of the specified {@link Collection} in parallel if
	 * supported by the underlying platform and the concrete implementation.
	 */
	public default void execute(Collection<? extends Runnable> tasks) {
		execute((Iterable<? extends Runnable>) tasks);
	}

	/**
	 * Executes all elements of the specified {@link Iterable} in parallel if
	 * supported by the underlying platform and the concrete implementation.
	 */
	public default void execute(Iterable<? extends Runnable> tasks) {
		if (tasks instanceof Collection) {
			execute((Collection<? extends Runnable>) tasks);
		} else {
			execute(tasks.iterator());
		}
	}

	/**
	 * Executes all elements of the specified {@link Iterator} in parallel if
	 * supported by the underlying platform and the concrete implementation.
	 */
	public abstract void execute(Iterator<? extends Runnable> tasks);

	/**
	 * Add a {@link Runnable} to this executor's batch. All runnables in the batch
	 * can later be run at once.
	 */
	public abstract void batch(Collection<Runnable> runnables);

	/**
	 * Executes all runnables in this executor's batch in parallel if supported by
	 * the underlying platform and the concrete implementation.
	 */
	public abstract void executeBatch();

}
