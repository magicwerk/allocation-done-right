/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.misc;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show that autoboxing and Integer.valueOf() create no new objects for small numbers which are hold in a global cache.
 * Otherwise an new Integer object must be allocated.
 */
public class Example_05_Integer {

	@State(Scope.Benchmark)
	public static class MyState {
		int cachedVal = 1;
		int uncachedVal = -1000;
	}

	@Benchmark
	public Integer testAutoboxCached(MyState state) {
		return state.cachedVal;
	}

	@Benchmark
	public Integer testAutoboxUncached(MyState state) {
		return state.uncachedVal;
	}

	@Benchmark
	public Integer testNewCached(MyState state) {
		return new Integer(state.cachedVal);
	}

	@Benchmark
	public Integer testNewUncached(MyState state) {
		return new Integer(state.uncachedVal);
	}

	@Benchmark
	public Integer testValueOfCached(MyState state) {
		return Integer.valueOf(state.cachedVal);
	}

	@Benchmark
	public Integer testValueOfUncached(MyState state) {
		return Integer.valueOf(state.uncachedVal);
	}
}