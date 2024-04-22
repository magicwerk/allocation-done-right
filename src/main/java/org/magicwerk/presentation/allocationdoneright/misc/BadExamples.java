package org.magicwerk.presentation.allocationdoneright.misc;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.tools.dev.jvm.HeapObserverState;
import org.magicwerk.presentation.allocationdoneright.PresentationHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Logger;

/**
 * Garbage Free Allocation.
 */
public class BadExamples {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationHelper tools = new PresentationHelper();

	public static void main(String[] args) {
		new BadExamples().run();
	}

	void run() {
		//HeapObserverState.configureActiveIteration(1);
		// new HeapObserverState().setupTrial();

		//CheckStringSplit.testSplitString();
		tools.runMeasureWithGc(CheckStringSplit.class);
	}

	/**
	 * Use a specialized SplitString class which keeps all information in a single chunk of memory.
	 * It outperforms the traditional approaches only if the splitted strings are not used. 
	 * 
	 * testSplitStringWithUse    5540864     368               
	 * testSplitStringWithoutUse 15352947    104
	 *                
	 * testStringSplit           13592509    200               
	 * testStringSplitter1       12421763    240               
	 * testStringSplitter2       12695230    224               
	 * testStringUtilsSplit      11102862    232   
	 */
	public static class CheckStringSplit {

		static abstract class StringSplitter {

			static class Builder {
				char c;
				String s;

				Builder setSplit(char c) {
					this.c = c;
					return this;
				}

				Builder setSplit(String s) {
					this.s = s;
					return this;
				}

				StringSplitter build() {
					if (s == null) {
						return new Splitter1(c);
					} else {
						return null;
					}
				}

			}

			IList<String> split(String s) {
				return getResult(s).splits;
			}

			abstract Result getResult(String s);

			static class Splitter1 extends StringSplitter {
				char c;

				Splitter1(char c) {
					this.c = c;
				}

				@Override
				Result getResult(String s) {
					Result r = new Result();
					int start = 0;
					int i;
					for (i = 0; i < s.length(); i++) {
						if (s.charAt(i) == c) {
							r.splits.add(s.substring(start, i));
							r.count++;
							i++;
							while (s.charAt(i) == c) {
								i++;
							}
						}
					}
					r.splits.add(s.substring(start, i));
					return r;
				}
			}

			static class Result {
				IList<String> splits = GapList.create();
				int count;
			}
		}

		static class Splitter1 {

			char c;

			Splitter1 setSplit(char c) {
				this.c = c;
				return this;
			}

			IList<String> split(String s) {
				return CheckStringSplit.split(s, c);
			}
		}

		static class SplitterWithBuilder {

			static class Builder {
				char c;

				Builder setSplit(char c) {
					this.c = c;
					return this;
				}

				SplitterWithBuilder build() {
					SplitterWithBuilder s = new SplitterWithBuilder();
					s.c = c;
					return s;
				}
			}

			char c;

			private SplitterWithBuilder() {
			}

			IList<String> split(String s) {
				return CheckStringSplit.split(s, c);
			}
		}

		/** Simple hand-written implementation string splitting */
		static IList<String> split(String s, char c) {
			// Method call allocates: 1 list + N strings
			IList<String> splits = GapList.create();
			int start = 0;
			int i;
			for (i = 0; i < s.length(); i++) {
				if (s.charAt(i) == c) {
					splits.add(s.substring(start, i));
					i++;
					while (s.charAt(i) == c) {
						i++;
					}
				}
			}
			splits.add(s.substring(start, i));
			return splits;
		}

		static void testSplitString() {
			String str = "a-b-c";
			char split = '-';
			LOG.info("{} (len= {})", str, str.length());

			IList<String> strs2 = split(str, split);
			LOG.info("{}", ReflectTools.getObjectSize(strs2));

			SplitString ss = splitString(str, split);
			LOG.info("{}", ReflectTools.getObjectSize(ss));

			GapList<Integer> vals = ss.buf.map(c -> (int) c);
			LOG.info("{}", vals);

			int num = ss.size();
			IList<String> strs = new GapList<>(num);
			for (int i = 0; i < num; i++) {
				strs.add(ss.get(i));
			}
			LOG.info("{}", strs);

		}

		static SplitString splitString(String s, char c) {
			return new SplitString(s, c);
		}

		static class SplitString {
			// Memory layout: num=3, len, index1, index0, 0, char0, char1, char2
			CharGapList buf = new CharGapList();

			SplitString(String str, char find) {
				int count = 0;
				int len = str.length();
				buf.addFirst((char) 0);
				for (int i = 0; i < len; i++) {
					char c = str.charAt(i);
					buf.addLast(c);
					if (c == find) {
						buf.addFirst((char) (i + 1));
						count++;
					}
				}
				buf.addFirst((char) (len + 1));
				buf.addFirst((char) (count + 1));
			}

			int size() {
				return buf.get(0);
			}

			String get(int index) {
				int offset = buf.get(0) + 2;
				// The indexes are in range 1..offset-1
				int start = buf.get(offset - index - 1);
				int end = buf.get(offset - index - 2) - 1;
				return new String(buf.toArray(), offset + start, end - start);
			}
		}

		@State(Scope.Benchmark)
		public static class CheckState {
			static final char SPLIT = ',';

			String str = "a,b,c";
			char split = ',';
		}

		@Benchmark
		public Object testSplitter(CheckState state) {
			return CheckStringSplit.split(state.str, state.split);
		}

		@Benchmark
		public Object testSplitter1(CheckState state) {
			Splitter1 ss = new Splitter1().setSplit(state.split);
			return ss.split(state.str);
		}

		@Benchmark
		public Object testSplitter2(CheckState state) {
			SplitterWithBuilder ss = new SplitterWithBuilder.Builder().setSplit(state.split).build();
			return ss.split(state.str);
		}

		static final SplitterWithBuilder swb = new SplitterWithBuilder.Builder().setSplit(CheckState.SPLIT).build();

		@Benchmark
		public Object testSplitter2b(CheckState state) {
			return swb.split(state.str);
		}

		/** Use org.apache.commons.lang3.StringUtils.split() */
		//@Benchmark
		public Object testStringUtilsSplit(CheckState state) {
			return StringUtils.split(state.str, state.split);
		}

		/** Use Simple hand-written implementation string splitting */
		//@Benchmark
		public Object testStringSplit(CheckState state) {
			return split(state.str, state.split);
		}

		/** Use StringSplitter with builder (created on each call) */
		//@Benchmark
		public Object testStringSplitter1(CheckState state, HeapObserverState hos) {
			StringSplitter ss = new StringSplitter.Builder().setSplit(state.split).build();
			return ss.split(state.str);
		}

		static final StringSplitter ss = new StringSplitter.Builder().setSplit(CheckState.SPLIT).build();

		/** Use StringSplitter with builder (created statically) */
		//@Benchmark
		public Object testStringSplitter2(CheckState state, HeapObserverState hos) {
			return ss.split(state.str);
		}

		//@Benchmark
		public Object testSplitStringWithoutUse(CheckState state) {
			return new SplitString(state.str, state.split);
		}

		//@Benchmark
		public int testSplitStringWithUse(CheckState state) {
			SplitString strs = new SplitString(state.str, state.split);
			int n = 0;
			for (int i = 0; i < strs.size(); i++) {
				String s = strs.get(i);
				n += s.length();
			}
			return n;
		}

	}

}
