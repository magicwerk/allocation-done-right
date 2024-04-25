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
public class Example_30_StatelessService {

	@State(Scope.Benchmark)
	public static class MyState {
		Counter1 counter1 = new Counter1();

		CyclicSource<String> strings = new CyclicSource<>(10, i -> "(" + i + "," + i + ")");
		CyclicSource<Character> finds = new CyclicSource<>(',', ':');
	}

	//

	@Benchmark
	public int test_0_StaticCall(MyState state) {
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
	public int test_1_Service(MyState state) {
		return state.counter1.count(state.strings.next(), state.finds.next());
	}

	@Benchmark
	public int test_2_Service_New(MyState state) {
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

	// Stateful Service

	@Benchmark
	public int test_3_StatefulService(MyState state) {
		StatefulService c = new StatefulService();
		c.count(state.strings.next(), state.finds.next());
		return c.get();
	}

	static class StatefulService {
		String str;
		char find;
		int count;

		int count(String str, char find) {
			this.str = str;
			this.find = find;

			count = 0;
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == find) {
					count++;
				}
			}
			return count;
		}

		int get() {
			return count;
		}
	}

	// Stateless Service

	@Benchmark
	public int test_4_StatelessService_Result(MyState state) {
		return new StatelessServie().getCountResult(state.strings.next(), state.finds.next()).count;
	}

	static class StatelessServie {

		static class Result {
			String str;
			char find;
			int count;
		}

		Result getCountResult(String str, char find) {
			Result r = new Result();
			r.str = str;
			r.find = find;
			process(r);
			return r;
		}

		void process(Result r) {
			r.count = count(r.str, r.find);
		}
	}

	//

	@Benchmark
	public int test_5_StatelessService_ResultOptions(MyState state) {
		StatelessService2.Options opts = new StatelessService2.Options().setFind(state.finds.next());
		StatelessService2 c = new StatelessService2(opts);
		return c.getCountResult(state.strings.next()).count;
	}

	static class StatelessService2 {
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

		StatelessService2(Options options) {
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