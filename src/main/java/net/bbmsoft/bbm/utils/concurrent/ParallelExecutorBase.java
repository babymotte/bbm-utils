package net.bbmsoft.bbm.utils.concurrent;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ParallelExecutorBase implements ParallelExecutor {

	private final Queue<Runnable> batch;

	public ParallelExecutorBase() {
		this.batch = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void batch(Collection<Runnable> runnables) {
		this.batch.addAll(runnables);
	}

	/**
	 * Run all {@link Runnable Runnables} that are currently batched by this
	 * executor. The exact behavior of this method depend's on the executor's
	 * concrete implementation of {@link #execute(Collection)}.
	 */
	@Override
	public void executeBatch() {
		this.execute(this.batch);
	}

}
