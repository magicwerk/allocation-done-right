package org.magicwerk.presentation.allocationdoneright.unused;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.strings.StringFormat;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Logger;

/**
 * Garbage Free Allocation.
 */
public class EvalExamples {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	/**
	 * Show that performance of Arrays.stream() is 10 times worse than the traditional approach.
	 * 
	 *                 Performance gc.alloc.rate.norm
	 * testArrayFor    341249663   0                 
	 * testArrayStream 23196243    184               
	 */
	public static class Eval_ArraysStream {

		@State(Scope.Benchmark)
		public static class CheckState {
			int[] array = new int[] { 0, 1, 2, 3, 4 };
		}

		@Benchmark
		public boolean testArrayStream(CheckState state) {
			return containsStream(state.array, 2);
		}

		boolean containsStream(int[] array, int find) {
			return Arrays.stream(array).anyMatch(m -> m == find);
		}

		@Benchmark
		public boolean testArrayFor(CheckState state) {
			return containsFor(state.array, 2);
		}

		boolean containsFor(int[] array, int find) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == find) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Show performance of string formatting.
	 */
	public static class EvalStringFormat {

		@State(Scope.Benchmark)
		public static class CheckState {
			String fmt = "({}-{})";
			String fmt2 = "(%s-%s)";
			String fmt3 = "({0}-{1})";

			StringFormat sf = new StringFormat(fmt);
			MessageFormat mf = new MessageFormat(fmt3);

			String s1 = "a";
			String s2 = "b";
		}

		@Benchmark
		public String testStringFormatterStatic(CheckState state) {
			return StringFormatter.format(state.fmt, state.s1, state.s2);
		}

		@Benchmark
		public String testStringFormatterCompiled(CheckState state) {
			return state.sf.format(state.fmt, state.s1, state.s2);
		}

		@Benchmark
		public String testStringFormatStatic(CheckState state) {
			return String.format(state.fmt2, state.s1, state.s2);
		}

		@Benchmark
		public String testMessageFormatStatic(CheckState state) {
			return MessageFormat.format(state.fmt3, state.s1, state.s2);
		}

		@Benchmark
		public String testMessageFormatCompiled(CheckState state) {
			return state.mf.format(new String[] { state.s1, state.s2 });
		}
	}

	/**
	 * Eval performance of String's replace(String) vs replace(char)
	 */
	public static class EvalReplace {

		@State(Scope.Benchmark)
		public static class CheckState {
			String str = "abc";
			String findStr = "b";
			String replaceStr = "B";
			char findChar = 'b';
			char replaceChar = 'B';
		}

		@Benchmark
		public String testReplaceString(CheckState state) {
			return state.str.replace(state.findStr, state.replaceStr);
		}

		@Benchmark
		public String testReplaceChar(CheckState state) {
			return state.str.replace(state.findChar, state.replaceChar);

			//		    public String replace(char oldChar, char newChar) {
			//	        if (oldChar != newChar) {
			//	            int len = value.length;
			//	            int i = -1;
			//	            char[] val = value; /* avoid getfield opcode */
		}
	}

	/**
	 * Eval performance of DateTimeFormatter vs SimpleDateFormat
	 */
	public static class EvalDateTimeFormatter {

		@State(Scope.Benchmark)
		public static class CheckState {
			ZonedDateTime time = ZonedDateTime.now();
			Date date = new Date();

			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}

		@Benchmark
		public String testDateTimeFormatter(CheckState state) {
			return state.timeFormatter.format(state.time);
		}

		@Benchmark
		public String testSimpleDateFormat(CheckState state) {
			return state.dateFormat.format(state.date);
		}

	}

}
