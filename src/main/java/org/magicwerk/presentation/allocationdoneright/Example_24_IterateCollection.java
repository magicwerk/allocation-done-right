/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show performance of iteration for:
 * - collection: for-each-loop, collection.forEach, collection.stream, collection.parallelStream
 * - list: for-index-loop, for-each-loop, list.forEach, list.stream, list.parallelStream.
 */
public class Example_24_IterateCollection {

	@State(Scope.Benchmark)
	public static class MyState {
		static final int SIZE = 100;
		List<String> list = IntStream.range(1, SIZE).boxed().map(String::valueOf).collect(Collectors.toList());
		Collection<String> coll = new HashSet<>(list);
		Collection<String> treeSet = new TreeSet<>(list);
	}

	// List

	@Benchmark
	public int test_List_Loop(MyState state) {
		int count = 0;
		for (int i = 0; i < state.list.size(); i++) {
			String n = state.list.get(i);
			count += n.length();
		}
		return count;
	}

	@Benchmark
	public int test_List_Iterable(MyState state) {
		int count = 0;
		for (String n : state.list) {
			count += n.length();
		}
		return count;
	}

	@Benchmark
	public int test_List_Iterator(MyState state) {
		int count = 0;
		for (Iterator<String> iter = state.list.iterator(); iter.hasNext();) {
			String n = iter.next();
			count += n.length();
		}
		return count;
	}

	@Benchmark
	public int test_List_ForEach(MyState state) {
		int[] count = new int[1];
		state.list.forEach(n -> {
			count[0] += n.length();
		});
		return count[0];
	}

	@Benchmark
	public int test_List_Stream(MyState state) {
		return (int) state.list.stream().filter(n -> n.length() > 1).count();
	}

	//@Benchmark
	public int test_List_ParallelStream(MyState state) {
		return (int) state.list.parallelStream().filter(n -> n.length() > 1).count();
	}

	// Collection

	//@Benchmark
	public int test_Collection_Iterable(MyState state) {
		int count = 0;
		for (String s : state.coll) {
			count += s.length();
		}
		return count;
	}

	//@Benchmark
	public int test_Collection_Iterator(MyState state) {
		int count = 0;
		for (Iterator<String> iter = state.coll.iterator(); iter.hasNext();) {
			String s = iter.next();
			count += s.length();
		}
		return count;
	}

	//@Benchmark
	public int test_Collection_ForEach(MyState state) {
		int[] count = new int[1];
		state.coll.forEach(s -> {
			count[0] += s.length();
		});
		return count[0];
	}

	//@Benchmark
	public int test_Collection_Stream(MyState state) {
		return (int) state.coll.stream().filter(n -> n.length() > 1).count();
	}

	//@Benchmark
	public int test_Collection_ParallelStream(MyState state) {
		return (int) state.coll.parallelStream().filter(n -> n.length() > 1).count();
	}

}