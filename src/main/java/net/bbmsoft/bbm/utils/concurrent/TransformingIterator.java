package net.bbmsoft.bbm.utils.concurrent;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class TransformingIterator<T, V> implements Iterator<V> {

	private final Iterator<T> wrappedIterator;
	private final Function<T, V> transformation;

	public TransformingIterator(Iterator<T> wrappedIterator, Function<T, V> transformation) {
		Objects.requireNonNull(wrappedIterator, "Delegate iterator cannot be null!");
		Objects.requireNonNull(transformation, "Transformation cannot be null!");
		this.wrappedIterator = wrappedIterator;
		this.transformation = transformation;
	}

	@Override
	public boolean hasNext() {
		return this.wrappedIterator.hasNext();
	}

	@Override
	public V next() {
		T next = this.wrappedIterator.next();
		V transformed = this.transformation.apply(next);
		return transformed;
	}

}
