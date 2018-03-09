package net.bbmsoft.bbm.utils.concurrent;

import java.util.Iterator;
import java.util.Objects;

public class CountingIterator<T> implements Iterator<T> {

	private final Iterator<T> wrappedIterator;

	private int counter;

	public CountingIterator(Iterator<T> wrappedIterator) {
		Objects.requireNonNull(wrappedIterator, "Delegate iterator cannot be null!");
		this.wrappedIterator = wrappedIterator;
		this.counter = -1;
	}

	@Override
	public boolean hasNext() {
		return this.wrappedIterator.hasNext();
	}

	@Override
	public T next() {
		this.counter++;
		return this.wrappedIterator.next();
	}

	public int getIndex() {
		return this.counter;
	}

}
