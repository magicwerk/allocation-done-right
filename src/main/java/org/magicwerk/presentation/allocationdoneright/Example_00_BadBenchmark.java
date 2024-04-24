package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.tools.dev.jvm.BlackSource;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Show show false benchmark design can make the benchmark senseless.
 */
public class Example_00_BadBenchmark {

	@Benchmark
	public void testVoid() {
		// Returned value is not used at all, call will be optimized away
		getErrorCode(true);
	}

	@Benchmark
	public int testInt() {
		// Returned value is consumed
		return getErrorCode(true);
	}

	@Benchmark
	public Object testInteger() {
		// Returned value 0 is converted to an Integer which can be taken out of the cache maintained by the Integer class
		return getErrorCode(false);
	}

	@Benchmark
	public Object testIntegerLarge() {
		// Returned value 1024 is converted to an Integer which must be allocated
		return getErrorCode(true);
	}

	static int getErrorCode(boolean error) {
		return (error) ? val1024.getInt() : val0.getInt();
	}

	static final BlackSource val0 = new BlackSource(0);
	static final BlackSource val1024 = new BlackSource(1024);

}