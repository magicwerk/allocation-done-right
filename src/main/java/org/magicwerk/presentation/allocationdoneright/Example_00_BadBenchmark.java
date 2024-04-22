/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * Show show false benchmark design (wrong use of return value / black hole) can make the benchmark senseless.
 */
public class Example_00_BadBenchmark {

	@Benchmark
	public int testInt() {
		return 1024;
	}

	@Benchmark
	public Object testObject() {
		// Convert 1024 to an Integer which must be allocated
		return 1024;
	}

	@Benchmark
	public Object testObjectSmall() {
		// Convert 1 to an Integer which can be taken out of the cache maintained by the Integer class
		return 1;
	}
}