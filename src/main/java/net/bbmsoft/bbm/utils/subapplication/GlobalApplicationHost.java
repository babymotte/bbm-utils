package net.bbmsoft.bbm.utils.subapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class GlobalApplicationHost extends Application {

	private static final AtomicBoolean created = new AtomicBoolean();
	private static final CountDownLatch launchLatch = new CountDownLatch(1);

	private static GlobalApplicationHost instance;

	private final Map<SubApplication, List<Window>> windows = new HashMap<>();
	private final Map<SubApplication, List<Dialog<?>>> dialogs = new HashMap<>();
	private final Map<SubApplication, CountDownLatch> countDownLatches = new HashMap<>();

	private boolean quitOnLastWindowClosed = true;

	@Override
	public void start(Stage primaryStage) throws Exception {

		synchronized (GlobalApplicationHost.class) {
			GlobalApplicationHost.instance = this;
		}

		GlobalApplicationHost.launchLatch.countDown();
	}

	@Override
	public void stop() throws Exception {
		synchronized (GlobalApplicationHost.class) {
			Set<SubApplication> subApps = new HashSet<>(GlobalApplicationHost.instance.windows.keySet());
			subApps.addAll(GlobalApplicationHost.instance.dialogs.keySet());
			subApps.forEach(app -> stopSubApp(app));
		}
	}

	public static void create(String... args) {

		boolean created = GlobalApplicationHost.created.getAndSet(true);

		if (!created) {

			Thread t = new Thread(() -> Application.launch(GlobalApplicationHost.class, args));
			t.setName("Global Application Host Thread");
			t.start();
		}

		try {
			GlobalApplicationHost.launchLatch.await();
		} catch (InterruptedException e) {
			return;
		}
	}

	public static <T extends SubApplication> void launchSubApplication(Class<T> subapplicationClass,
			Consumer<T> instanceConsumer) {

		launchSubApplication(() -> subapplicationClass.newInstance(), instanceConsumer);
	}
	
	public static <T extends SubApplication> void launchSubApplication(Class<T> subapplicationClass,
			Consumer<T> instanceConsumer, boolean block) {

		launchSubApplication(() -> subapplicationClass.newInstance(), instanceConsumer, block);
	}

	public static <T extends SubApplication> void launchSubApplication(UnsafeInstanceSupplier<T> instanceSupplier,
			Consumer<T> instanceConsumer) {
		launchSubApplication(instanceSupplier, instanceConsumer, true);
	}

	public static <T extends SubApplication> void launchSubApplication(UnsafeInstanceSupplier<T> instanceSupplier,
			Consumer<T> instanceConsumer, boolean block) {

		GlobalApplicationHost.create();

		synchronized (GlobalApplicationHost.class) {

			if (GlobalApplicationHost.instance == null) {
				throw new IllegalStateException(
						"Global Application Host not running! Call GlobalApplicationHost.create(...) before starting subapplications.");
			}

		}

		CountDownLatch latch = block ? new CountDownLatch(1) : null;

		Platform.runLater(() -> {
			T instance;
			try {
				instance = instanceSupplier.getInstance();
				launchSubApplication(instance, latch);
				if (instanceConsumer != null) {
					instanceConsumer.accept(instance);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		});

		if (latch != null) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public static void launchSubApplication(SubApplication subapplication) {
		launchSubApplication(subapplication, null);
	}

	private static void launchSubApplication(SubApplication subapplication, CountDownLatch latch) {

		GlobalApplicationHost.create();

		synchronized (GlobalApplicationHost.class) {

			if (GlobalApplicationHost.instance == null) {
				throw new IllegalStateException(
						"Global Application Host not running! Call GlobalApplicationHost.create(...) before starting subapplications.");
			}

		}

		Platform.runLater(() -> instance._implLaunchSubApplication(subapplication, latch));

	}

	static void quitSubApplication(SubApplication subapplication) {

		synchronized (GlobalApplicationHost.class) {

			if (GlobalApplicationHost.instance == null) {
				throw new IllegalStateException(
						"Global Application Host not running! Call GlobalApplicationHost.create(...) before starting subapplications.");
			}
		}

		if (Platform.isFxApplicationThread()) {
			instance._implQuitSubApplication(subapplication);
		} else {
			CountDownLatch latch = new CountDownLatch(1);

			Platform.runLater(() -> {
				try {
					instance._implQuitSubApplication(subapplication);
				} finally {
					latch.countDown();
				}
			});

			try {
				latch.await();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	static void registerWindow(SubApplication subapplication, Window window) {

		synchronized (GlobalApplicationHost.class) {

			if (GlobalApplicationHost.instance == null) {
				throw new IllegalStateException(
						"Global Application Host not running! Call GlobalApplicationHost.create(...) before starting subapplications.");
			}
		}
		
		instance._implRegisterWindow(subapplication, window);

	}

	static void registerDialog(SubApplication subapplication, Dialog<?> dialog) {

		synchronized (GlobalApplicationHost.class) {

			if (GlobalApplicationHost.instance == null) {
				throw new IllegalStateException(
						"Global Application Host not running! Call GlobalApplicationHost.create(...) before starting subapplications.");
			}
		}

		instance._implRegisterDialog(subapplication, dialog);

	}

	private void _implLaunchSubApplication(SubApplication subapplication, CountDownLatch latch) {

		if (this.windows.containsKey(subapplication)) {
			throw new IllegalStateException(
					String.format("SubApplication %s has already been launched!", String.valueOf(subapplication)));
		}

		Stage window = new Stage();
		List<Window> windows = new ArrayList<>();
		List<Dialog<?>> dialogs = new ArrayList<>();

		this.windows.put(subapplication, windows);
		this.dialogs.put(subapplication, dialogs);
		if (latch != null) {
			this.countDownLatches.put(subapplication, latch);
		}

		registerWindow(subapplication, window);

		try {
			subapplication.start(window);
		} catch (Exception e) {
			System.err.println("Error starting sub application!");
			e.printStackTrace();
			this.windows.remove(subapplication);
			this.dialogs.remove(subapplication);
		}
	}

	private void windowClosed(SubApplication subapplication, Window window) {

		_implUnregisterWindow(subapplication, window);

		boolean notQuit = this.windows.get(subapplication) != null;
		List<Window> allWindows = this.windows.get(subapplication);
		boolean allWindowsClosed = allWindows.isEmpty();

		if (notQuit && allWindowsClosed && this.quitOnLastWindowClosed) {
			_implQuitSubApplication(subapplication);
		}
	}

	private void dialogClosed(SubApplication subapplication, Dialog<?> dialog) {

		_implUnregisterDialog(subapplication, dialog);

		boolean notQuit = this.windows.get(subapplication) != null;
		List<Window> allWindows = this.windows.get(subapplication);
		List<Dialog<?>> allDialogs = this.dialogs.get(subapplication);
		boolean allWindowsClosed = allWindows.isEmpty();
		boolean allDialogsClosed = allDialogs.isEmpty();

		if (notQuit && allWindowsClosed && allDialogsClosed && this.quitOnLastWindowClosed) {
			_implQuitSubApplication(subapplication);
		}
	}

	private void _implQuitSubApplication(SubApplication subapplication) {

		if (!this.windows.containsKey(subapplication)) {
			throw new IllegalStateException(
					String.format("SubApplication %s has not been launched!", String.valueOf(subapplication)));
		}

		List<Dialog<?>> dialogs = this.dialogs.remove(subapplication);
		dialogs.forEach(d -> d.close());

		List<Window> windows = this.windows.remove(subapplication);
		windows.forEach(w -> w.hide());

		stopSubApp(subapplication);
	}

	private void _implRegisterWindow(SubApplication subapplication, Window window) {

		if (!this.windows.containsKey(subapplication)) {
			throw new IllegalStateException(
					String.format("SubApplication %s has not been launched!", String.valueOf(subapplication)));
		}

		window.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> windowClosed(subapplication, window));
		this.windows.get(subapplication).add(window);
	}

	private void _implUnregisterWindow(SubApplication subapplication, Window window) {

		if (!this.windows.containsKey(subapplication)) {
			throw new IllegalStateException(
					String.format("SubApplication %s has not been launched!", String.valueOf(subapplication)));
		}

		this.windows.get(subapplication).remove(window);
	}

	private boolean _implRegisterDialog(SubApplication subapplication, Dialog<?> dialog) {

		if (!this.dialogs.containsKey(subapplication)) {
			return false;
		}

		dialog.setOnCloseRequest(e -> dialogClosed(subapplication, dialog));
		return this.dialogs.get(subapplication).add(dialog);
	}

	private boolean _implUnregisterDialog(SubApplication subapplication, Dialog<?> dialog) {

		if (!this.dialogs.containsKey(subapplication)) {
			return false;
		}

		return this.dialogs.get(subapplication).remove(dialog);
	}

	private void stopSubApp(SubApplication subapplication) {

		try {
			subapplication.stop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CountDownLatch latch = this.countDownLatches.remove(subapplication);
			if (latch != null) {
				latch.countDown();
			}
		}
	}
}
