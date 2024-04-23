/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.misc;

import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

// https://stackoverflow.com/questions/58696093/when-does-jvm-start-to-omit-stack-traces
// OmitStackTraceInFastThrow is an optimization in hotspot that uses preallocated exceptions in highly optimized code. 
// This saves the time to allocate the exception object. But the preallocated exceptions have no message nor a stack trace.
// Code that has exception handling that gets hot enough to get optimized this way is a flawy design. 
public class Example_07_ExceptionOmitStackTrace {

	@State(Scope.Benchmark)
	public static class MyState {
		static final int ERROR_RATE = 10;
		int count;
		Supplier<String> supplier = () -> (count++ % ERROR_RATE == 0) ? null : "123";
	}

	@Benchmark
	public int testOmitStackTrace(MyState state) {
		String str = state.supplier.get();
		try {
			return str.length();
		} catch (NullPointerException e) {
			if (state.count == 1_000_001) {
				e.printStackTrace();
			}
			return -1;
		}
	}

	@Fork(jvmArgs = "-XX:-OmitStackTraceInFastThrow")
	@Benchmark
	public int testDisableOmitStackTrace(MyState state) {
		String str = state.supplier.get();
		try {
			return str.length();
		} catch (NullPointerException e) {
			if (state.count == 1_000_001) {
				e.printStackTrace();
			}
			return -1;
		}
	}
}