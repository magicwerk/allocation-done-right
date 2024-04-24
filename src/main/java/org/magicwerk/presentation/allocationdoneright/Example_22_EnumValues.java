/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.concurrent.TimeUnit;

import org.magicwerk.brownies.tools.dev.jvm.BlackHole;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Show use of Enum.values()
 */
public class Example_22_EnumValues {

	/** No allocation necessary, array does not escape and is not changed */
	@Benchmark
	public int testEnumValues1() {
		TimeUnit[] values = TimeUnit.values();
		return values.length;
	}

	/** No allocation necessary, array is changed but JIT is able to figure out that this change has no effect */
	@Benchmark
	public int testEnumValues2() {
		TimeUnit[] values = TimeUnit.values();
		values[0] = values[1];
		return values.length;
	}

	/** Allocation is necessary, array escapes from method */
	@Benchmark
	public int testEnumValues3() {
		TimeUnit[] values = TimeUnit.values();
		BlackHole.consume(values);
		return values.length;
	}

}