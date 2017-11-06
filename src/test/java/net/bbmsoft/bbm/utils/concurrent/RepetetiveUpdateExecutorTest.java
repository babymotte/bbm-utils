package net.bbmsoft.bbm.utils.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;

import net.bbmsoft.bbm.utils.concurrent.RepetitiveUpdateExecutor;

public class RepetetiveUpdateExecutorTest {

	private Executor thread = Executors.newSingleThreadExecutor();

	private RepetitiveUpdateExecutor exec = new RepetitiveUpdateExecutor(thread);

	@Test
	public void runTest() throws InterruptedException {

//		thread.execute(() -> {
//			System.out.println("Starting long runnig task in worker thread");
//			try {
//				Thread.sleep(3000);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println("Long running task in worker thread done.");
//		});

		int loops = 20;
		for (int i = 0; i < loops; i++) {
			int count = i;
			exec.execute(() -> action(count), 200);
		}

		// wait for worker to complete
		Thread.sleep(8000);
	}

	private void action(int counter) {
		System.out.printf("Pass %d: Current time: %d\n", Integer.valueOf(counter), Long.valueOf(System.currentTimeMillis()));
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
}
