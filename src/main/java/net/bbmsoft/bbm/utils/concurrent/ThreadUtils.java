package net.bbmsoft.bbm.utils.concurrent;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;

public class ThreadUtils {

	public static void runOnJavaFXThread(Runnable run) {

		if (Platform.isFxApplicationThread()) {
			run.run();
			return;
		}

		Platform.runLater(() -> {
			run.run();
		});
	}

	public static void runOnJavaFXThreadAndWait(Runnable run) throws InterruptedException {

		if (Platform.isFxApplicationThread()) {
			run.run();
			return;
		}

		CountDownLatch latch = new CountDownLatch(1);

		Platform.runLater(() -> {
			try {
				run.run();
			} finally {
				latch.countDown();
			}
		});

		latch.await();
	}

	public static void checkFxThread() {
		if (!Platform.isFxApplicationThread()) {
			throw new IllegalStateException(
					"Not on JavaFX Application Thread! Current thread is " + Thread.currentThread().getName());
		}
	}
}
