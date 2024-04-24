package org.magicwerk.presentation.allocationdoneright;

import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.BlackHole;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationFreeRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationObserverState;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;

import ch.qos.logback.classic.Logger;

/**
 * Show how to check that a method is allocation free.
 */
public class Example_19_AllocationFreeTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new Example_19_AllocationFreeTest().testParseIntAllocationFree();
	}

	@Capture(source = Source.NONE)
	public void testParseIntAllocationFree() {
		Options opts = new Options().includeClass(TestParseIntAllocationFree.class).setRunTimeMillis(100).setSourceDir("src/main/java");
		new JmhAllocationFreeRunner().checkAllocationFree(opts);
		Report.printCapture("parseInt is allocation free");
	}

	public static class TestParseIntAllocationFree {

		@Benchmark
		public int testAllocationFree() {
			int sum = 0;
			sum += parseInt("1");
			sum += parseInt("12");
			return sum;
		}

		//@Benchmark
		public int testNotAllocationFree() {
			BlackHole.consume(new Object()); // bad

			int sum = 0;
			sum += parseInt("1");
			sum += parseInt("12");
			return sum;
		}

		//@Benchmark
		public int testShowNotAllocationFree(JmhAllocationObserverState state) {
			BlackHole.consume(new Object()); // bad

			int sum = 0;
			sum += parseInt("1");
			sum += parseInt("12");
			return sum;
		}
	}

	@Trace
	public void testParseInt() {
		parseInt("1");
		parseInt("12");
		parseInt("x");
	}

	static Integer parseInt(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return null;
			}
		}
		return Integer.parseInt(str);
	}

}
