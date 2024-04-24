/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.unused;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * Show that arbitrary changes can influence escape analysis. <br>
 * - the short loop is allocation free for all versions, the longer for {@literal >=} Java 17
 */
public class Example_02_Autoboxing_Bad {

	@Benchmark
	public int test1() {
		Integer i = calc(10);
		return i;
	}

	@Benchmark
	public int test2() {
		Integer i = calc(1000);
		return i;
	}

	Integer calc(int iter) {
		int sum = 0;
		for (int i = 0; i < iter; i++) {
			sum += i;
		}
		return sum;
	}
}