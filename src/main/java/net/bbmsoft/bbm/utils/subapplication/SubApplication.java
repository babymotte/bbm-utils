package net.bbmsoft.bbm.utils.subapplication;

import java.util.function.Consumer;

import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class SubApplication {

	public abstract void start(Stage primaryStage) throws Exception;

	public void stop() {
	}

	public final void registerWindow(Window window) {
		GlobalApplicationHost.registerWindow(this, window);
	}

	public final void registerDialog(Dialog<?> dialog) {
		GlobalApplicationHost.registerDialog(this, dialog);
	}

	public final void quit() {
		GlobalApplicationHost.quitSubApplication(this);
	}

	public static void launch(SubApplication subApplication) {
		GlobalApplicationHost.launchSubApplication(subApplication);
	}

	public static void launch(Class<? extends SubApplication> clazz) {
		GlobalApplicationHost.launchSubApplication(clazz, null);
	}
	
	public static <T extends SubApplication> void launch(UnsafeInstanceSupplier<T> instanceSupplier) {
		GlobalApplicationHost.launchSubApplication(instanceSupplier, null);
	}
	
	public static <T extends SubApplication> void launch(UnsafeInstanceSupplier<T> instanceSupplier, boolean block) {
		GlobalApplicationHost.launchSubApplication(instanceSupplier, null, block);
	}
	
	public static <T extends SubApplication> void launch(Class<T> clazz, Consumer<T> instanceConsumer) {
		GlobalApplicationHost.launchSubApplication(clazz, instanceConsumer);
	}
	
	public static void launch() {
		Consumer<SubApplication> consumer = null;
		launch(consumer);
	}

	@SuppressWarnings("unchecked")
	public static <T extends SubApplication> void launch(Consumer<T> instanceConsumer) {

		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String callingClassName = trace[2].getClassName();
		Class<?> clazz;
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(callingClassName);
		} catch (ClassNotFoundException e) {
			System.err.println("Failed to load sub application class " + callingClassName);
			e.printStackTrace();
			return;
		}

		if(SubApplication.class.isAssignableFrom(clazz)) {
			GlobalApplicationHost.launchSubApplication((Class<T>)clazz, instanceConsumer);
		} else {
			throw new IllegalStateException(String.format("%s does not extend %s!", clazz.getName(), SubApplication.class.getName()));
		}
	}
}
