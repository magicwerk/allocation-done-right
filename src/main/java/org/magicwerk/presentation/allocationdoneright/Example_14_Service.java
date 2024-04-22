package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Show that a service implementation with options and result class does not produce any garbage
 * and is as fast as a single static call.
 * 
 * allocation-free for {@literal >=} Java 17
 */
public class Example_14_Service {

	@State(Scope.Benchmark)
	public static class MyState {
		Counter1 counter1 = new Counter1();

		CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + "," + i + ")");
		CyclicSource<Character> finds = new CyclicSource<>(',', ':');
	}

	//

	@Benchmark
	public int test_1_StaticCall(MyState state) {
		return count(state.strings.next(), state.finds.next());
	}

	static int count(String str, char find) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == find) {
				count++;
			}
		}
		return count++;
	}

	//

	@Benchmark
	public int test_2_InstanceCall(MyState state) {
		return state.counter1.count(state.strings.next(), state.finds.next());
	}

	@Benchmark
	public int test_2_InstanceCall_New(MyState state) {
		return new Counter1().count(state.strings.next(), state.finds.next());
	}

	static class Counter1 {
		int count(String str, char find) {
			int count = 0;
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == find) {
					count++;
				}
			}
			return count++;
		}
	}

	//

	@Benchmark
	public int test_2b_InstanceCall_New(MyState state) {
		Counter5 c = new Counter5();
		c.count(state.strings.next(), state.finds.next());
		return c.get();
	}

	static class Counter5 {
		int count;

		void count(String str, char find) {
			count = 0;
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == find) {
					count++;
				}
			}
		}

		int get() {
			return count;
		}
	}

	//

	@Benchmark
	public int test_3_Counter_Result(MyState state) {
		return new Counter().getCountResult(state.strings.next(), state.finds.next()).count;
	}

	static class Counter {

		static class Result {
			//char find;
			int count;
		}

		Result getCountResult(String str, char find) {
			Result r = new Result();
			//r.find = find;
			r.count = count(str, find);
			return r;
		}
	}

	//

	@Benchmark
	public int test_4_Counter_ResultOptions(MyState state) {
		Counter2.Options opts = new Counter2.Options().setFind(state.finds.next());
		Counter2 c = new Counter2(opts);
		return c.getCountResult(state.strings.next()).count;
	}

	static class Counter2 {
		static class Options {
			char find;

			Options setFind(char find) {
				this.find = find;
				return this;
			}
		}

		static class Result {
			Options options;
			int count;
		}

		Options options;

		Counter2(Options options) {
			this.options = options;
		}

		Result getCountResult(String str) {
			Result r = new Result();
			r.options = options;
			r.count = count(str, options.find);
			return r;
		}
	}
}