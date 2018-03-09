package net.bbmsoft.bbm.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ConcatenatedList<T> implements List<T> {

	private final List<? extends T> a;
	private final List<? extends T> b;

	public ConcatenatedList(List<? extends T> a, List<? extends T> b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public int size() {
		return this.a.size() + this.b.size();
	}

	@Override
	public boolean isEmpty() {
		return this.a.isEmpty() && this.b.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.a.contains(o) || this.b.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatenatedIterator(this.a.iterator(), this.b.iterator());
	}

	@Override
	public Object[] toArray() {
		Object[] out = new Object[this.size()];
		int i = 0;
		for (T t : this) {
			out[i++] = t;
		}
		return out;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V[] toArray(V[] a) {

		int size = this.size();

		V[] r = a.length >= size ? a : (V[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

		int i = 0;
		for (T t : this) {
			r[i++] = (V) t;
		}

		return r;
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public boolean containsAll(Collection<?> c) {

		for (T t : this) {
			if (t.equals(c)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public T get(int index) {

		if (index < this.a.size()) {
			return this.a.get(index);
		} else {
			return this.b.get(index);
		}
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException("Concatenated lists are read-only!");
	}

	@Override
	public int indexOf(Object o) {

		int index;
		if ((index = a.indexOf(o)) >= 0) {
			return index;
		} else {
			return b.indexOf(o);
		}
	}

	@Override
	public int lastIndexOf(Object o) {

		int index;
		if ((index = b.lastIndexOf(o)) >= 0) {
			return index;
		} else {
			return a.lastIndexOf(o);
		}
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ConcatenatedListIterator(this.a.listIterator(), this.b.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(int index) {

		if (index < this.a.size()) {
			return new ConcatenatedListIterator(this.a.listIterator(index), this.b.listIterator());
		} else {
			return new ConcatenatedListIterator(this.a.listIterator(this.a.size()),
					this.b.listIterator(index - this.a.size()));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> subList(int fromIndex, int toIndex) {

		if (fromIndex < this.a.size() && toIndex <= this.a.size()) {
			return (List<T>) this.a.subList(fromIndex, toIndex);
		}

		if (fromIndex < this.a.size() && toIndex > this.a.size()) {
			return new ConcatenatedList<>(this.a.subList(fromIndex, this.a.size()),
					this.b.subList(0, toIndex - this.a.size()));
		}

		if (fromIndex >= this.a.size() && toIndex > this.a.size()) {
			return (List<T>) this.b.subList(fromIndex - this.a.size(), toIndex - this.a.size());
		}

		throw new IndexOutOfBoundsException();
	}

	class ConcatenatedIterator implements Iterator<T> {

		private Iterator<? extends T> a;
		private Iterator<? extends T> b;

		public ConcatenatedIterator(Iterator<? extends T> a, Iterator<? extends T> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean hasNext() {
			return this.a.hasNext() || this.b.hasNext();
		}

		@Override
		public T next() {
			return this.a.hasNext() ? this.a.next() : this.b.next();
		}

	}

	class ConcatenatedListIterator extends ConcatenatedIterator implements ListIterator<T> {

		private ListIterator<? extends T> a;
		private ListIterator<? extends T> b;

		public ConcatenatedListIterator(ListIterator<? extends T> a, ListIterator<? extends T> b) {
			super(a, b);
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean hasPrevious() {
			return this.b.hasPrevious() || this.a.hasPrevious();
		}

		@Override
		public T previous() {
			return this.b.hasPrevious() ? this.b.previous() : this.a.previous();
		}

		@Override
		public int nextIndex() {
			return this.a.hasNext() ? this.a.nextIndex() : this.b.nextIndex();
		}

		@Override
		public int previousIndex() {
			return this.b.hasPrevious() ? this.b.previousIndex() : this.a.previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Concatenated lists are read-only!");
		}

		@Override
		public void set(T e) {
			throw new UnsupportedOperationException("Concatenated lists are read-only!");
		}

		@Override
		public void add(T e) {
			throw new UnsupportedOperationException("Concatenated lists are read-only!");
		}

	}
}
