package net.bbmsoft.bbm.utils.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

	private final String name;
	private final boolean daemon;
	private final AtomicInteger counter;
	private final int priority;

	public CustomThreadFactory(String name, boolean daemon) {
		this.name = name;
		this.daemon = daemon;
		this.counter = new AtomicInteger();
		this.priority = Thread.NORM_PRIORITY;
	}

	public CustomThreadFactory(String name, boolean daemon, int priority) {
		this.name = name;
		this.daemon = daemon;
		this.priority = priority;
		this.counter = new AtomicInteger();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setName(this.name + " - " + this.counter.incrementAndGet());
		t.setDaemon(this.daemon);
		t.setPriority(this.priority);
		return t;
	}
}
