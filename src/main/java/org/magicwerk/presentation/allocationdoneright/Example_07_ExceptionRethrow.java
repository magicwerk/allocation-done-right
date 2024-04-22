/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Supplier;

import org.magicwerk.brownies.core.ExceptionTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show memory and performance costs of rethrowing vs wrapping exceptions. 
 * Note that gc.alloc.rate.norm of testWrapAndThrow is double of testRewthrow.
 */
public class Example_07_ExceptionRethrow {

	@State(Scope.Benchmark)
	public static class MyState {
		int count;
		Supplier<String> supplier = () -> (count++ % 10 == 0) ? "abc" : "123";
	}

	@Benchmark
	public int testRewthrow(MyState state) {
		try {
			String str = state.supplier.get();
			return parseRewthrow(str);
		} catch (Exception e) {
			return -1;
		}
	}

	@Benchmark
	public int testWrapAndThrow(MyState state) {
		try {
			String str = state.supplier.get();
			return parseWrapAndThrow(str);
		} catch (Exception e) {
			return -1;
		}
	}

	static int parseRewthrow(String str) {
		try {
			return parse(str);
		} catch (NumberFormatException e) {
			throw ExceptionTools.throwException(e);
		}
	}

	static int parseWrapAndThrow(String str) {
		try {
			return parse(str);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}
	}

	static int parse(String str) {
		throw new NumberFormatException(str);
	}

}