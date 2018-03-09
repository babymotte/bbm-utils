package net.bbmsoft.bbm.utils.concurrent;

public interface ObservableParallelExecutor extends ParallelExecutor {

	public interface ProgressMonitor {

		public void updateProgress(double done, double total);

		public default void updateMessage(String message) {
		}

		public default void onError(String message, boolean critical) {
		}

	}

	public void setProgressMonitor(ProgressMonitor monitor);
}
