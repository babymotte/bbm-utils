package net.bbmsoft.bbm.utils.concurrent;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public interface TaskHelper {

	public <T> void submitTask(Task<T> task, Consumer<T> resultConsumer,
			Consumer<Exception> exceptionhandler);

	public default <T> void submitTask(Task<T> task, Consumer<T> resultConsumer) {
		submitTask(task, resultConsumer, null);
	}

	public default <T> Task<T> createAndSubmitTask(Supplier<T> supplier, String label, Consumer<T> resultConsumer,
			Consumer<Exception> exceptionhandler) {

		Task<T> task = new Task<T>() {
			
			{
				updateTitle(label);
			}

			@Override
			protected T call() throws Exception {
				return supplier.get();
			}
		};
		submitTask(task, resultConsumer, exceptionhandler);
		return task;
	}

	public default <T> Task<T> createAndSubmitTask(Supplier<T> supplier, String label, Consumer<T> resultConsumer) {
		return createAndSubmitTask(supplier, label, resultConsumer, null);
	}

	public default Task<?> createAndSubmitTask(Runnable runnable, String label, Consumer<Exception> exceptionhandler) {
		return createAndSubmitTask(() -> {
			runnable.run();
			return null;
		}, label, Object -> {}, exceptionhandler);
	}
	
	public default Task<?> createAndSubmitTask(Runnable runnable, String label) {
		return createAndSubmitTask(runnable, label, null);
	}
	
	public ObservableList<Task<?>> getTaskList();

	public void setTaskList(ObservableList<Task<?>> taskList);
}
