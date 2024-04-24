/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

// Exceptions general:
// https://belief-driven-design.com/all-you-ever-wanted-to-know-about-java-exceptions-63d838fedb3/
// - Use flag -XX:-OmitStackTraceInFastThrow to guarantee that stacktrace is always available
// - Use constructor Throwable(String, Throwable, boolean boolean) to prevent calling fillInStackTrace()
// - Using a static exception to avoid creating an new instance for each occurrence
/**
 * Show memory and performance costs of exceptions. 
 */
public class Example_23_Exception {

	@State(Scope.Benchmark)
	public static class MyState {
		static final int ERROR_RATE = 10;
		int count;
		Supplier<String> supplier = () -> (count++ % ERROR_RATE == 0) ? "abc" : "123";
	}

	@Benchmark
	public int testWithNull(MyState state) {
		String str = state.supplier.get();
		Integer num = parseWithNull(str);
		return (num != null) ? num : -1;
	}

	@Benchmark
	public int testWithException(MyState state) {
		String str = state.supplier.get();
		try {
			int num = parseWithException(str);
			return num;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Benchmark
	public int testWithExceptionWithoutStackTrace(MyState state) {
		String str = state.supplier.get();
		try {
			int num = parseWithExceptionWithoutStackTrace(str);
			return num;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Benchmark
	public int testWithExceptionWithoutStackTraceAsSingleton(MyState state) {
		String str = state.supplier.get();
		try {
			int num = parseWithExceptionAsSingleton(str);
			return num;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** Parse integer, return null if invalid */
	static Integer parseWithNull(String str) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int n = c - '0';
			if (n < 0 || n > 9) {
				return null;
			}
			num = 10 * num + n;
		}
		return num;
	}

	/** Parse integer, throw exception if invalid (like {@link Integer#parseInt}) */
	static int parseWithException(String str) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int n = c - '0';
			if (n < 0 || n > 9) {
				throw new NumberFormatException(str);
			}
			num = 10 * num + n;
		}
		return num;
	}

	/** Parse integer, throw exception without stack trace if invalid */
	static int parseWithExceptionWithoutStackTrace(String str) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int n = c - '0';
			if (n < 0 || n > 9) {
				throw new NumberFormatExceptionWithoutStackTrace(str);
			}
			num = 10 * num + n;
		}
		return num;
	}

	/** Parse integer, throw singleton exception without stack trace if invalid */
	static int parseWithExceptionAsSingleton(String str) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int n = c - '0';
			if (n < 0 || n > 9) {
				throw NumberFormatExceptionWithoutStackTrace.SINGLETON;
			}
			num = 10 * num + n;
		}
		return num;
	}

	static class NumberFormatExceptionWithoutStackTrace extends NumberFormatException {

		static final NumberFormatExceptionWithoutStackTrace SINGLETON = new NumberFormatExceptionWithoutStackTrace(null);

		public NumberFormatExceptionWithoutStackTrace(String msg) {
			super(msg);
		}

		@Override
		public Throwable fillInStackTrace() {
			return this;
		}
	}

}