/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show that auto boxing/unboxing primitives/wrappers can be optimized away. <br>
 * - testBoolean: allocation-free for {@literal >=} Java 8
 * - testInteger: allocation-free for {@literal >=} Java 17
 */
public class Example_20_Autoboxing {

	@State(Scope.Benchmark)
	public static class MyState {
		int count;
		Supplier<Boolean> supplyBoolean = () -> (count++ % 2 == 0) ? true : false;
		Supplier<Integer> supplyInt = () -> count++;
	}

	@Benchmark
	public boolean testBoolean(MyState state) {
		boolean b = getBoolean(state);
		return b;
	}

	Boolean getBoolean(MyState state) {
		return state.supplyBoolean.get();
	}

	@Benchmark
	public int testInteger(MyState state) {
		int i = getInteger(state);
		return i;
	}

	Integer getInteger(MyState state) {
		return state.supplyInt.get();
	}
}