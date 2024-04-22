/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import ch.qos.logback.classic.Logger;

/**
 * Show that allocations executed concurrently do not scale.
 * The same benchmark allocating simple objects is called concurrently for different number of threads.
 */
// See for TLAB settings:
// https://www.baeldung.com/java-jvm-tlab#bd-tuning-tlab-settings
public class Example_09_ConcurrentAllocation {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	@State(Scope.Benchmark)
	public static class MyState {
		static {
			LOG.info("availableProcessors: {}", SystemTools.getNumAvailableProcessors());
		}
	}

	@Threads(1)
	@Benchmark
	public Object testThreads1(MyState state) {
		return new Object();
	}

	@Fork(jvmArgs = "-XX:-UseTLAB")
	@Threads(1)
	@Benchmark
	public Object testThreads1NoTLAB(MyState state) {
		return new Object();
	}

	@Threads(2)
	@Benchmark
	public Object testThreads2(MyState state) {
		return new Object();
	}

	@Fork(jvmArgs = "-XX:-UseTLAB")
	@Threads(2)
	@Benchmark
	public Object testThreads2NoTLAB(MyState state) {
		return new Object();
	}

	@Threads(4)
	@Benchmark
	public Object testThreads4(MyState state) {
		return new Object();
	}

	@Threads(6)
	@Benchmark
	public Object testThreads6(MyState state) {
		return new Object();
	}

	@Threads(8)
	@Benchmark
	public Object testThreads8(MyState state) {
		return new Object();
	}

	@Threads(12)
	//@Benchmark
	public Object testThreads12(MyState state) {
		return new Object();
	}
}