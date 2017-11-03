package net.bbmsoft.bbm.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.bbmsoft.bbm.utils.concurrent.ParallelExecutor;

public class ParallelExecutorTest {

	@Test
	public void testForeachParallelWithIndex() {

		ParallelExecutor exec = new ParallelExecutor() {

			@Override
			public void executeBatch() {
			}

			@Override
			public void execute(Iterator<? extends Runnable> tasks) {
				tasks.forEachRemaining(t -> t.run());
			}

			@Override
			public void batch(Collection<Runnable> runnables) {
			}
		};

		exec.forEachParallel(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (item, index) -> assertEquals(item, index));
	}

}
