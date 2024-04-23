/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import ch.qos.logback.classic.Logger;

public class Example_11_StringBuilder {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new Example_11_StringBuilder().run();
	}

	/**
	 * Default length of StringBuilder is only 16 characters.
	 */
	void run() {
		int LEN = 1000;
		testStringBuilderGrow(LEN, 100, 0);
		testStringBuilderGrow(LEN, 100, 256);
	}

	void testStringBuilderGrow(int totalLen, int addLen, int preSize) {
		String add = StringTools.repeat("x", addLen);

		int oldCapacity = 0;
		int numGrow = 0;
		int num = totalLen / addLen;
		StringBuilder buf = (preSize > 0) ? new StringBuilder(preSize) : new StringBuilder();
		for (int i = 0; i < num; i++) {
			int capacity = buf.capacity();
			if (capacity > oldCapacity) {
				oldCapacity = capacity;
				numGrow++;
			}
			LOG.info("length= {}, capacity= {}", buf.length(), capacity);
			buf.append(add);
		}
		LOG.info("numGrow= {}\n", numGrow);
	}

	//

	static final int NUM = 10;
	static final int LEN = 100;
	static final String STR = StringTools.repeat("x", LEN);

	//

	@State(Scope.Benchmark)
	public static class MyState {
		IList<String> strs = GapList.create();

		public MyState() {
			for (int i = 0; i < 10; i++) {
				strs.add(STR);
			}
		}
	}

	@Benchmark
	public String test_1_StringAdd(MyState state) {
		String result = "";
		for (String str : state.strs) {
			result += str;
		}
		return result;
	}

	@Benchmark
	public String test_2_StringBuilder(MyState state) {
		StringBuilder result = new StringBuilder();
		for (String str : state.strs) {
			result.append(str);
		}
		return result.toString();
	}

	@Benchmark
	public String test_3_StringBuilderPresized(MyState state) {
		StringBuilder result = new StringBuilder(1000);
		for (String str : state.strs) {
			result.append(str);
		}
		return result.toString();
	}

	//

	static final StringBuilder STRING_BUILDER = new StringBuilder();

	@Benchmark
	public String test_4_Static(MyState state) {
		StringBuilder result = STRING_BUILDER;
		result.setLength(0);
		for (String str : state.strs) {
			result.append(str);
		}
		return result.toString();
	}

	//

	static final ThreadLocal<StringBuilder> STRING_BUILDER_HOLDER = ThreadLocal.withInitial(StringBuilder::new);

	@Benchmark
	public String test_5_ThreadLocal(MyState state) {
		StringBuilder result = STRING_BUILDER_HOLDER.get();
		result.setLength(0);
		for (String str : state.strs) {
			result.append(str);
		}
		return result.toString();
	}

	//

	static class StringBuilderPool {
		BlockingQueue<StringBuilder> pool = new LinkedBlockingQueue<>();

		public StringBuilder get() {
			StringBuilder buf = pool.poll();
			if (buf == null) {
				buf = new StringBuilder();
			} else {
				buf.setLength(0);
			}
			return buf;
		}

		public void release(StringBuilder buf) {
			pool.add(buf);
		}
	}

	static final StringBuilderPool STRING_BUILDER_POOL = new StringBuilderPool();

	@Benchmark
	public String test_6_Pool(MyState state) {
		StringBuilder buf = STRING_BUILDER_POOL.get();
		buf.setLength(0);
		for (String str : state.strs) {
			buf.append(str);
		}
		String result = buf.toString();
		STRING_BUILDER_POOL.release(buf);
		return result;
	}

	//

	@Threads(2)
	@Benchmark
	public String test_5_ThreadLocal_Parallel_2(MyState state) {
		return test_5_ThreadLocal(state);
	}

	@Threads(2)
	@Benchmark
	public String test_6_Pool_Parallel_2(MyState state) {
		return test_6_Pool(state);
	}

	@Threads(Threads.MAX)
	@Benchmark
	public String test_5_ThreadLocal_Parallel_Max(MyState state) {
		return test_5_ThreadLocal(state);
	}

	@Threads(Threads.MAX)
	@Benchmark
	public String test_6_Pool_Parallel_Max(MyState state) {
		return test_6_Pool(state);
	}

}