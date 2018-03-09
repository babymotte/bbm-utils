package net.bbmsoft.bbm.utils.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

public class SimpleTaskHelper implements TaskHelper {

	private final ExecutorService executor;

	private ObservableList<Task<?>> activeTasks;

	public SimpleTaskHelper(ObservableList<Task<?>> activeTasks, ExecutorService executor) {
		this.activeTasks = activeTasks;
		this.executor = executor;
	}

	public SimpleTaskHelper(ExecutorService executor) {
		this.activeTasks = FXCollections.observableArrayList();
		this.executor = executor;
	}

	@Override
	public <T> void submitTask(Task<T> task, Consumer<T> resultConsumer, Consumer<Exception> exceptionhandler) {

		if (this.activeTasks.contains(task)) {
			return;
		}

		task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, e -> this.activeTasks.remove(task));
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, e -> this.activeTasks.remove(task));
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
			this.activeTasks.remove(task);
			try {
				T result = task.get();
				if (resultConsumer != null) {
					resultConsumer.accept(result);
				}
			} catch (InterruptedException | ExecutionException e1) {
				if (exceptionhandler != null) {
					exceptionhandler.accept(e1);
				} else {
					e1.printStackTrace();
				}
			}
		});

		this.activeTasks.add(task);

		this.executor.submit(task);
	}

	@Override
	public ObservableList<Task<?>> getTaskList() {
		return activeTasks;
	}

	@Override
	public void setTaskList(ObservableList<Task<?>> activeTasks) {
		java.util.Objects.requireNonNull(activeTasks);
		this.activeTasks = activeTasks;
	}

}
