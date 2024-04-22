package org.magicwerk.presentation.allocationdoneright;

import org.magictest.client.Capture;
import org.magictest.client.Capture.Source;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhAllocationRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Test of class {@link TypeTools}
 */
public class AllocationFreeTest {

	public static void main(String[] args) {
		new AllocationFreeTest().testParseIntAllocationFree();
	}

	@Capture(source = Source.NONE)
	public void testParseIntAllocationFree() {
		//new JmhAllocationRunner().checkForAllAllocationFreeResult(TestParseIntAllocationFree.class);

		Options opts = new Options().includeClass(TestParseIntAllocationFree.class);
		new JmhAllocationRunner().showAllocationResults(opts);
	}

	public static class TestParseIntAllocationFree {
		@Benchmark
		public int test() {
			int sum = 0;
			sum += TypeTools.parseInt("1");
			sum += TypeTools.parseInt("12");
			//Blackhole.consume(Integer.valueOf(1000));
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

	//

	public static Integer parseInt(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return null;
			}
		}
		return Integer.parseInt(str);
	}

}
