package org.magicwerk.presentation.allocationdoneright.misc;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show that iteration of IntObjGapList is allocation-free for {@literal >=} Java 21
 */
public class Example_04_IterateCollection_IntObjGapList {

	@State(Scope.Benchmark)
	public static class MyState {
		IList<Integer> list1 = GapList.create();
		IntObjGapList list2 = new IntObjGapList();
		{
			for (int i = 0; i < 1000; i++) {
				list1.add(i);
				list2.add(i);
			}
		}
	}

	@Benchmark
	public int testIList(MyState state) {
		int sum = 0;
		for (Integer val : state.list1) {
			sum += val;
		}
		return sum;
	}

	@Benchmark
	public int testIntObjGapList(MyState state) {
		int sum = 0;
		for (Integer val : state.list2) {
			sum += val;
		}
		return sum;
	}

}