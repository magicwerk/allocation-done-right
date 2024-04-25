package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Predicate;

import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.collections.Sources.CyclicSource;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.print.PrintTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.dev.jvm.JmhState;
import org.magicwerk.presentation.allocationdoneright.Example_32_CheckFramework.CheckFramework.MultiMode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Logger;

/**
 * Garbage Free Allocation.
 */
public class Example_32_CheckFramework {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static class CheckFrameworkTestBase {

		@State(Scope.Benchmark)
		public static class MyState {
			CyclicSource<String> empty = new CyclicSource<>("");
			CyclicSource<String> nonEmpty = new CyclicSource<>("a", "b");
		}

		static boolean isEmpty(String s) {
			return s == null || s.isEmpty();
		}

		static boolean isNonEmpty(String s) {
			return s != null && !s.isEmpty();
		}

		static <T> Predicate<T> not(Predicate<T> target) {
			return target.negate();
		}

		public static Predicate<String> predicateEmpty() {
			return StringTools::isEmpty;
		}

		public static Predicate<String> predicateNonEmpty() {
			return s -> !StringTools.isEmpty(s);
		}
	}

	public static class CheckFrameworkTest extends CheckFrameworkTestBase {

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck1(MyState state) {
			CheckFramework.single(CheckFrameworkTestBase::isEmpty)
					.check(state.empty.next());
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck2(MyState state) {
			CheckFramework.single(CheckFrameworkTestBase::isEmpty).withMessage("fails")
					.check(state.empty.next());
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck3(MyState state) {
			CheckFramework.single(CheckFrameworkTestBase::isEmpty).withMessage("error: {}", "details")
					.check(state.empty.next());
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck4(MyState state) {
			CheckFramework.single(not(CheckFrameworkTestBase::isEmpty))
					.check(state.nonEmpty.next());
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck5(MyState state) {
			CheckFramework.single(CheckFrameworkTestBase::isNonEmpty)
					.check(state.nonEmpty.next());
		}

		// allocation-free for >= Java 8
		@Benchmark
		public void testCheck6(MyState state) {
			CheckFramework.single(predicateEmpty())
					.check(state.empty.next());
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testCheck7(MyState state) {
			CheckFramework.single(predicateEmpty().negate())
					.check(state.nonEmpty.next());
		}

		// allocation-free for >= Java 21
		@Benchmark
		public void testCheckMulti(MyState state) {
			CheckFramework.multi(not(StringTools::isEmpty), MultiMode.ALL).withMessage("error: {}", "details")
					.check(state.nonEmpty.next(), state.nonEmpty.next());
		}

		// allocation-free for >= Java 17
		@Benchmark
		public void testChecMultikStatic(MyState state) {
			checkStatic.check(state.nonEmpty.next(), state.nonEmpty.next());
		}

		static CheckFramework.CheckMulti<String> checkStatic = // 
				CheckFramework.multi(not(StringTools::isEmpty), MultiMode.ALL).withMessage("error: {}", "details");
	}

	public static class CheckFrameworkTestExample extends CheckFrameworkTestBase {

		@Fork(jvmArgs = { "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-verbose:gc" })
		@Benchmark
		public void testCheckMulti(MyState state, JmhState js) {
			String s1 = state.nonEmpty.next();
			String s2 = state.nonEmpty.next();

			// Let one call fail
			if (js.getMeasurementIteration() == 0 && js.getInvocation() == 100_000_000) {
				s2 = "";
			}

			try {
				CheckFramework.multi(not(StringTools::isEmpty), MultiMode.ALL).withMessage("error: {}", "details").check(s1, s2);
			} catch (Exception e) {
				LOG.info("ERROR: {}", e.getMessage());
			}
		}
	}

	public static class CheckFramework {

		public enum MultiMode {
			ANY,
			ALL
		}

		public static <T> CheckSingle<T> single(Predicate<T> predicate) {
			return new CheckSingle<>(predicate);
		}

		public static <T> CheckMulti<T> multi(Predicate<T> predicate, MultiMode mode) {
			return new CheckMulti<>(predicate, mode);
		}

		public static class CheckBase<E, T extends CheckBase<E, ?>> {
			Predicate<E> predicate;
			String msg;
			Object[] args;

			@SuppressWarnings("unchecked")
			public T withMessage(String msg) {
				this.msg = msg;
				return (T) this;
			}

			@SuppressWarnings("unchecked")
			public T withMessage(String format, Object... args) {
				this.msg = format;
				this.args = args;
				return (T) this;
			}

			void error(Object predicate) {
				String str = (args != null) ? StringFormatter.format(msg, args) : msg;
				if (str == null) {
					str = predicate.toString() + ": " + PrintTools.toString(args);
				}
				CheckTools.error(str);
			}
		}

		public static class CheckSingle<E> extends CheckBase<E, CheckSingle<E>> {

			CheckSingle(Predicate<E> predicate) {
				this.predicate = predicate;
			}

			public E check(E val) {
				if (predicate.test(val)) {
					return val;
				}
				error(predicate);
				return null;
			}
		}

		public static class CheckMulti<E> extends CheckBase<E, CheckMulti<E>> {

			MultiMode mode;

			CheckMulti(Predicate<E> predicate, MultiMode mode) {
				this.predicate = predicate;
				this.mode = mode;
			}

			@SuppressWarnings("unchecked")
			public void check(E... vals) {
				if (mode == MultiMode.ANY) {
					for (E val : vals) {
						if (predicate.test(val)) {
							return;
						}
					}
					error(predicate);
				} else if (mode == MultiMode.ALL) {
					for (E val : vals) {
						if (!predicate.test(val)) {
							error(predicate);
						}
					}
				} else {
					assert false;
				}
			}
		}
	}

}
