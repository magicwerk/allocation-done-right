/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.misc;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import ch.qos.logback.classic.Logger;

public class Sample_11_StringBuilder_Encode {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new Sample_11_StringBuilder_Encode().run();
	}

	/**
	 * Default length of StringBuilder is only 16 characters.
	 */
	void run() {
		testStringBuilderGrow(10);
		testStringBuilderGrow(100);
	}

	void testStringBuilderGrow(int len) {
		String add = StringTools.repeat("x", len);
		int oldCapacity = 0;
		int numGrow = 0;
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			int capacity = buf.capacity();
			if (capacity > oldCapacity) {
				oldCapacity = capacity;
				numGrow++;
			}
			LOG.info("length= {}, capacity= {}", buf.length(), capacity);
			buf.append(add);
		}
		LOG.info("numGrow= {}", numGrow);
	}

	@State(Scope.Benchmark)
	public static class MyState {
		ThreadLocal<StringBuilder> threadLocal = ThreadLocal.withInitial(StringBuilder::new);
		StringBuilder buf = new StringBuilder();
		IList<String> strs = GapList.create();

		public MyState() {
			for (int i = 0; i < 10; i++) {
				String str = StringTools.repeat("x", 10 * i) + "\"" + StringTools.repeat("x", 10 * i);
				strs.add(str);
			}
		}
	}

	@Benchmark
	public String testFormat1(MyState state) {
		return format1(state.strs);
	}

	@Benchmark
	public String testFormat2(MyState state) {
		return format2(state.strs);
	}

	@Benchmark
	public String testFormat3(MyState state) {
		return format3(state.strs);
	}

	@Benchmark
	public String testFormat4(MyState state) {
		return format3(state.buf, state.strs);
	}

	@Benchmark
	public String testFormat5(MyState state) {
		StringBuilder buf = state.threadLocal.get();
		return format3(buf, state.strs);
	}

	//@Benchmark
	@Threads(Threads.MAX)
	public String testFormat2P(MyState state) {
		return format2(state.strs);
	}

	//@Benchmark
	@Threads(Threads.MAX)
	public String testFormat3P(MyState state) {
		return format3(state.strs);
	}

	String format1(IList<String> strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.size(); i++) {
			buf.append(i + ": ");
			buf.append(encode1(strs.get(i)));
		}
		return buf.toString();
	}

	String format2(IList<String> strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.size(); i++) {
			buf.append(i + ": ");
			buf.append(encode2(strs.get(i)));
		}
		return buf.toString();
	}

	String format3(IList<String> strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.size(); i++) {
			buf.append(i + ": ");
			encode3(buf, strs.get(i));
		}
		return buf.toString();
	}

	String format3(StringBuilder buf, IList<String> strs) {
		buf.setLength(0);
		for (int i = 0; i < strs.size(); i++) {
			buf.append(i + ": ");
			encode3(buf, strs.get(i));
		}
		return buf.toString();
	}

	//@Benchmark
	public int testEncode1(MyState state) {
		int len = 0;
		for (int i = 0; i < state.strs.size(); i++) {
			String str = encode1(state.strs.get(i));
			len += str.length();
		}
		return len;
	}

	//@Benchmark
	public int testEncode2(MyState state) {
		int len = 0;
		for (int i = 0; i < state.strs.size(); i++) {
			String str = encode2(state.strs.get(i));
			len += str.length();
		}
		return len;
	}

	/** Encode with newly allocated StringBuilder (with default size) */
	String encode1(String str) {
		StringBuilder buf = new StringBuilder();
		buf.append('"');
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"') {
				buf.append("\"\"");
			} else {
				buf.append(c);
			}
		}
		buf.append('"');
		return buf.toString();
	}

	/** Encode with newly allocated StringBuilder (presized) */
	String encode2(String str) {
		int size = 2 * str.length() + 2;
		StringBuilder buf = new StringBuilder(size);
		buf.append('"');
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"') {
				buf.append("\"\"");
			} else {
				buf.append(c);
			}
		}
		buf.append('"');
		return buf.toString();
	}

	/** Encode with passed StringBuilder */
	void encode3(StringBuilder buf, String str) {
		buf.append('"');
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"') {
				buf.append("\"\"");
			} else {
				buf.append(c);
			}
		}
		buf.append('"');
	}

}