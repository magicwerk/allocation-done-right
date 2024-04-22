package org.magicwerk.presentation.allocationdoneright;

import java.util.function.Supplier;

import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.dev.jvm.HeapObserverState;
import org.magicwerk.presentation.allocationdoneright.Example_10_FluentLogging.BaseLogger.LogLevel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Test different approaches for logging.
 * Show that a fluent logger can be as fast as 
 */
public class Example_10_FluentLogging {

	@State(Scope.Benchmark)
	public static class CheckState {

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
		public void StringLogger_EnabledFormat(CheckState state) {
			state.stringLogger.info(StringFormatter.format("info-{}", state.getCount()));
		}

		@Benchmark
		public void StringLogger_EnabledString(CheckState state) {
			state.stringLogger.info(state.getMessage());
		}

		@Benchmark
		public void StringLogger_DisabledFormat(CheckState state) {
			state.stringLogger.debug(StringFormatter.format("debug-{}", state.getCount()));
		}

		@Benchmark
		public void StringLogger_DisabledString(CheckState state) {
			state.stringLogger.debug(state.getMessage());
		}

		//

		@Benchmark
		public void FormatLogger_EnabledFormat(CheckState state) {
			state.formatLogger.info("info-{}", state.getCount());
		}

		@Benchmark
		public void FormatLogger_EnabledString(CheckState state) {
			state.formatLogger.info(state.getMessage());
		}

		@Benchmark
		public void FormatLogger_DisabledFormat(CheckState state) {
			state.formatLogger.debug("debug-{}", state.getCount());
		}

		@Benchmark
		public void FormatLogger_DisabledString(CheckState state) {
			state.formatLogger.debug(state.getMessage());
		}

		//

		@Benchmark
		public void SupplierLogger_EnabledFormat(CheckState state) {
			state.supplierLogger.info(() -> StringFormatter.format("info-{}", state.getCount()));
		}

		@Benchmark
		public void SupplierLogger_EnabledString(CheckState state) {
			state.supplierLogger.info(() -> state.getMessage());
		}

		@Benchmark
		public void SupplierLogger_DisabledFormat(CheckState state) {
			state.supplierLogger.debug(() -> StringFormatter.format("debug-{}", state.getCount()));
		}

		@Benchmark
		public void SupplierLogger_DisabledString(CheckState state) {
			state.supplierLogger.debug(() -> state.getMessage());
		}

		//

		@Benchmark
		public void FluentLogger_EnabledFormat(CheckState state) {
			state.fluentLogger.info().log("info-{}", state.getCount());
		}

		@Benchmark
		public void FluentLogger_EnabledString(CheckState state) {
			state.fluentLogger.info().log(state.getMessage());
		}

		@Benchmark
		public void FluentLogger_DisabledFormat(CheckState state) {
			state.fluentLogger.debug().log("debug-{}", state.getCount());
		}

		@Benchmark
		public void FluentLogger_DisabledString(CheckState state) {
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

		static final NoLogger noLogger = new NoLogger();

		public Log info() {
			return (isActive(LogLevel.INFO)) ? new RealLogger(this) : noLogger;
		}

		public Log debug() {
			return (isActive(LogLevel.DEBUG)) ? new RealLogger(this) : noLogger;
		}
	}

}
