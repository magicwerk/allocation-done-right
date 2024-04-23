/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show how arrays, lists, and collections can be treated in an uniform way using a wrapper which is fully optimized away by the JVM.
 * 
 * allocation-free for {@literal >=} Java 8
 */
public class Example_06_Listable {

	interface ICollectable<E> {
		int size();

		Iterator<E> iterator();
	}

	interface IListable<E> {
		int size();

		E get(int index);
	}

	public static class ArrayIterator<E> implements Iterator<E> {

		E[] array;
		int index;

		ArrayIterator(E[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public E next() {
			if (index >= array.length) {
				throw new NoSuchElementException();
			}
			return array[index++];
		}

	}

	protected static class IListableArray<E> implements IListable<E>, ICollectable<E> {
		E[] array;

		IListableArray(E[] array) {
			this.array = array;
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public E get(int index) {
			return array[index];
		}

		@Override
		public Iterator<E> iterator() {
			return new ArrayIterator<>(array);
		}
	}

	protected static class IListableList<E> implements IListable<E>, ICollectable<E> {
		List<E> list;

		IListableList(List<E> list) {
			this.list = list;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public E get(int index) {
			return list.get(index);
		}

		@Override
		public Iterator<E> iterator() {
			return list.iterator();
		}
	}

	protected static class IListableCollection<E> implements ICollectable<E> {
		Collection<E> coll;

		IListableCollection(Collection<E> coll) {
			this.coll = coll;
		}

		@Override
		public int size() {
			return coll.size();
		}

		@Override
		public Iterator<E> iterator() {
			return coll.iterator();
		}
	}

	@State(Scope.Benchmark)
	public static class MyState {
		final int size = 1000;
		IList<Integer> list = new GapList<>(size);
		Integer[] array = new Integer[size];

		public MyState() {
			for (int i = 0; i < size; i++) {
				list.add(i);
				array[i] = i;
			}
		}
	}

	@Benchmark
	public int handleArray(MyState state) {
		return handleArray(state.array);
	}

	@Benchmark
	public int handleList(MyState state) {
		return handleList(state.list);
	}

	@Benchmark
	public int handleCollection(MyState state) {
		return handleCollection(state.list);
	}

	@Benchmark
	public int handleListListable(MyState state) {
		return handleListable(new IListableList<>(state.list));
	}

	@Benchmark
	public int handleListCollectable(MyState state) {
		return handleCollectable(new IListableList<>(state.list));
	}

	@Benchmark
	public int handleArrayListable(MyState state) {
		return handleListable(new IListableArray<>(state.array));
	}

	@Benchmark
	public int handleArraysAsList(MyState state) {
		return handleList(Arrays.asList(state.array));
	}

	@Benchmark
	public int handleArrayCollectable(MyState state) {
		return handleCollectable(new IListableArray<>(state.array));
	}

	@Benchmark
	public int handleCollectionCollectable(MyState state) {
		return handleCollectable(new IListableCollection<>(state.list));
	}

	@Benchmark
	public int handleListToArray(MyState state) {
		return handleArray(state.list.toArray(Integer.class));
	}

	int handleArray(Integer[] list) {
		int sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i].intValue();
		}
		return sum;
	}

	int handleList(List<Integer> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i).intValue();
		}
		return sum;
	}

	int handleCollection(Collection<Integer> coll) {
		int sum = 0;
		for (Iterator<Integer> iter = coll.iterator(); iter.hasNext();) {
			sum += iter.next().intValue();
		}
		return sum;
	}

	int handleListable(IListable<Integer> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i).intValue();
		}
		return sum;
	}

	int handleCollectable(ICollectable<Integer> list) {
		int sum = 0;
		for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {
			sum += iter.next().intValue();
		}
		return sum;
	}

	int handleIterable(Iterable<Integer> list) {
		int sum = 0;
		for (Integer val : list) {
			sum += val.intValue();
		}
		return sum;
	}
}