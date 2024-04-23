package org.magicwerk.presentation.allocationdoneright;

import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.tools.dev.jvm.BlackHole;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationFreeRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationObserverState;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;

import ch.qos.logback.classic.Logger;

/**
 * Test of class {@link TypeTools}
 */
public class AllocationFreeTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new AllocationFreeTest().testParseIntAllocationFree();
	}

	@Capture(source = Source.NONE)
	public void testParseIntAllocationFree() {
		Options opts = new Options().includeClass(TestParseIntAllocationFree.class).setRunTimeMillis(100);
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
			int sum = 0;
			sum += parseIntBad("1");
			sum += parseIntBad("12");
			return sum;
		}

		//@Benchmark
		public int testShowNotAllocationFree(JmhAllocationObserverState state) {
			int sum = 0;
			sum += parseIntBad("1");
			sum += parseIntBad("12");
			return sum;
		}
	}

	@Trace
	public void testParseInt() {
		parseInt("1");
		parseInt("12");
		parseInt("-1");
		parseInt("+1");
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

	static Integer parseIntBad(String str) {
		BlackHole.consume(new Object());
		return parseInt(str);
	}

}
