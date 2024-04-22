package org.magicwerk.presentation.allocationdoneright;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class Example_14_Service2 {

	@State(Scope.Benchmark)
	public static class CheckState {
		String str = "-----A-----B-----";
		Example_14_Service2.S0 s0 = new S0();
		Example_14_Service2.S1 s1 = new S1();
		Example_14_Service2.S2 s2 = new S2();
		Example_14_Service2.StatefulService statefulService = new StatefulService();
		Example_14_Service2.StatelessService statelessService = new StatelessService();
		Example_14_Service2.StatelessServiceBroken statelessServiceBroken = new StatelessServiceBroken();
	}

	//@Benchmark
	public int testS0(CheckState state) {
		return state.s0.count(state.str);
	}

	//@Benchmark
	public int testS1(CheckState state) {
		return state.s1.count(state.str);
	}

	//@Benchmark
	public int testS2(CheckState state) {
		return state.s2.count(state.str);
	}

	//

	@Benchmark
	public int testStatefulService(CheckState state) {
		return state.statefulService.count(state.str);
	}

	@Benchmark
	public int testStatelessService(CheckState state) {
		return state.statelessService.count(state.str).count();
	}

	@Benchmark
	public int testStatelessServiceBroken(CheckState state) {
		return state.statelessServiceBroken.count(state.str).count();
	}

	/** Implementation with local variables */
	static class S0 {

		int count(String str) {
			int count = 0;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == 'A') {
					count++;
				}
			}
			return count;
		}
	}

	/** Implementation with local variables */
	static class S1 {
		String str;
		int count;

		int count(String str) {
			this.str = str;
			this.count = doCount(str);
			return count;
		}

		int doCount(String str) {
			int count = 0;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == 'A') {
					count++;
				}
			}
			return count;
		}
	}

	/** Implementation with state in service */
	static class S2 {
		String str;
		int count;

		int count(String str) {
			this.str = str;
			this.count = 0;
			doCount();
			return count;
		}

		void doCount() {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == 'A') {
					count++;
				}
			}
		}
	}

	/** Implementation with state allocated context */
	static class S2b {

		static class Context {
			String str;
			int count;
		}

		S2b.Context count(String str) {
			S2b.Context ctx = new Context();
			ctx.str = str;
			doCount(ctx);
			return ctx;
		}

		void doCount(S2b.Context ctx) {
			for (int i = 0; i < ctx.str.length(); i++) {
				char c = ctx.str.charAt(i);
				if (c == 'A') {
					ctx.count++;
				}
			}
		}
	}

	//
	static class StatefulService {
		String str;
		int countA;
		int countB;

		int count(String str) {
			this.str = str;
			this.countA = 0;
			this.countB = 0;

			doCount();
			return count();
		}

		void doCount() {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == 'A') {
					countA++;
				} else if (c == 'B') {
					countB++;
				}
			}
		}

		int count() {
			return countA + countB;
		}

		int countA() {
			return countA;
		}

		int countB() {
			return countB;
		}
	}

	static class StatelessService {

		static class Context {
			String str;
			int countA;
			int countB;

			int count() {
				return countA + countB;
			}
		}

		StatelessService.Context count(String str) {
			StatelessService.Context ctx = new Context();
			ctx.str = str;
			doCount(ctx);
			return ctx;
		}

		void doCount(StatelessService.Context ctx) {
			startContext(ctx);

			for (int i = 0; i < ctx.str.length(); i++) {
				char c = ctx.str.charAt(i);
				if (c == 'A') {
					ctx.countA++;
				} else if (c == 'B') {
					ctx.countB++;
				}
			}
		}

		void startContext(StatelessService.Context ctx) {
		}
	}

	static class StatelessServiceBroken extends StatelessService {

		@Override
		void startContext(StatelessService.Context ctx) {
			//Blackhole.consume(ctx);

			// Also simply calling hashCode() breaks JVM optization 
			//ctx.hashCode();
			//System.identityHashCode(ctx);

			// But logging should be typically be ok
			Presentation.LOG.debug("{}", ctx);
		}
	}

}