package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Supplier;

import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.dev.jvm.HeapObserverState;
import org.magicwerk.presentation.allocationdoneright.Example_31_FluentLogging.BaseLogger.LogLevel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Test different approaches for logging.
 * Show that a fluent logger can be as faster than a traditional implementation.
 */
public class Example_31_FluentLogging {

	@State(Scope.Benchmark)
	public static class MyState {

		StringLogger stringLogger = new StringLogger();
		SupplierLogger supplierLogger = new SupplierLogger();
		FormatLogger formatLogger = new FormatLogger();
		FluentLogger fluentLogger = new FluentLogger();

		int count;

		@Setup(Level.Trial)
		public void setupTrial() {
			HeapObserverState.setActive(false);
			HeapObserverState.setActiveIteration(5);

			stringLogger.setLevel(LogLevel.INFO);
			formatLogger.setLevel(LogLevel.INFO);
			supplierLogger.setLevel(LogLevel.INFO);
			fluentLogger.setLevel(LogLevel.INFO);

			count = 0;
		}

		int getCount() {
			return count++;
		}

		String getMessage() {
			return "message";
		}

		//

		@Benchmark
		public void StringLogger_EnabledFormat(MyState state) {
			state.stringLogger.info(StringFormatter.format("info-{}", state.getCount()));
		}

		@Benchmark
		public void StringLogger_EnabledString(MyState state) {
			state.stringLogger.info(state.getMessage());
		}

		@Benchmark
		public void StringLogger_DisabledFormat(MyState state) {
			state.stringLogger.debug(StringFormatter.format("debug-{}", state.getCount()));
		}

		@Benchmark
		public void StringLogger_DisabledString(MyState state) {
			state.stringLogger.debug(state.getMessage());
		}

		//

		@Benchmark
		public void FormatLogger_EnabledFormat(MyState state) {
			state.formatLogger.info("info-{}", state.getCount());
		}

		@Benchmark
		public void FormatLogger_EnabledString(MyState state) {
			state.formatLogger.info(state.getMessage());
		}

		@Benchmark
		public void FormatLogger_DisabledFormat(MyState state) {
			state.formatLogger.debug("debug-{}", state.getCount());
		}

		@Benchmark
		public void FormatLogger_DisabledString(MyState state) {
			state.formatLogger.debug(state.getMessage());
		}

		//

		@Benchmark
		public void SupplierLogger_EnabledFormat(MyState state) {
			state.supplierLogger.info(() -> StringFormatter.format("info-{}", state.getCount()));
		}

		@Benchmark
		public void SupplierLogger_EnabledString(MyState state) {
			state.supplierLogger.info(() -> state.getMessage());
		}

		@Benchmark
		public void SupplierLogger_DisabledFormat(MyState state) {
			state.supplierLogger.debug(() -> StringFormatter.format("debug-{}", state.getCount()));
		}

		@Benchmark
		public void SupplierLogger_DisabledString(MyState state) {
			state.supplierLogger.debug(() -> state.getMessage());
		}

		//

		@Benchmark
		public void FluentLogger_EnabledFormat(MyState state) {
			state.fluentLogger.info().log("info-{}", state.getCount());
		}

		@Benchmark
		public void FluentLogger_EnabledString(MyState state) {
			state.fluentLogger.info().log(state.getMessage());
		}

		@Benchmark
		public void FluentLogger_DisabledFormat(MyState state) {
			state.fluentLogger.debug().log("debug-{}", state.getCount());
		}

		@Benchmark
		public void FluentLogger_DisabledString(MyState state) {
			state.fluentLogger.debug().log(state.getMessage());
		}

	}

	//

	static abstract class BaseLogger {

		enum LogLevel {
			DEBUG,
			INFO
		}

		LogLevel level;

		void setLevel(LogLevel level) {
			this.level = level;
		}

		boolean isActive(LogLevel level) {
			return level.ordinal() >= this.level.ordinal();
		}

		void log(String str) {

		}
	}

	static class StringLogger extends BaseLogger {

		public void info(String str) {
			if (isActive(LogLevel.INFO)) {
				log(str);
			}
		}

		public void debug(String str) {
			if (isActive(LogLevel.DEBUG)) {
				log(str);
			}
		}
	}

	static class FormatLogger extends BaseLogger {

		public void info(String str) {
			if (isActive(LogLevel.INFO)) {
				log(str);
			}
		}

		public void debug(String str) {
			if (isActive(LogLevel.DEBUG)) {
				log(str);
			}
		}

		public void info(String fmt, Object... args) {
			if (isActive(LogLevel.INFO)) {
				log(StringFormatter.format(fmt, args));
			}
		}

		public void debug(String fmt, Object... args) {
			if (isActive(LogLevel.DEBUG)) {
				log(StringFormatter.format(fmt, args));
			}
		}
	}

	static class SupplierLogger extends BaseLogger {

		public void info(Supplier<String> supplier) {
			if (isActive(LogLevel.INFO)) {
				log(supplier.get());
			}
		}

		public void debug(Supplier<String> supplier) {
			if (isActive(LogLevel.DEBUG)) {
				log(supplier.get());
			}
		}
	}

	static class FluentLogger extends BaseLogger {

		public Log info() {
			return (isActive(LogLevel.INFO)) ? new RealLogger(this) : noLogger;
		}

		public Log debug() {
			return (isActive(LogLevel.DEBUG)) ? new RealLogger(this) : noLogger;
		}

		static final NoLogger noLogger = new NoLogger();

		interface Log {
			void log(String fmt, Object... args);

			void log(String str);
		}

		static class NoLogger implements Log {
			@Override
			public void log(String fmt, Object... args) {
			}

			@Override
			public void log(String str) {
			}
		}

		static class RealLogger implements Log {
			BaseLogger baseLogger;

			RealLogger(BaseLogger baseLogger) {
				this.baseLogger = baseLogger;
			}

			@Override
			public void log(String fmt, Object... args) {
				baseLogger.log(StringFormatter.format(fmt, args));
			}

			@Override
			public void log(String str) {
				baseLogger.log(str);
			}
		}

	}

}
