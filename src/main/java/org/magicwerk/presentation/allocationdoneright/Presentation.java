package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.objects.Tuple;
import org.magicwerk.brownies.tools.dev.jvm.HeapObserver;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.brownies.tools.dev.memory.MemoryHistoryMonitoring.MemoryReporter;

import ch.qos.logback.classic.Logger;

/**
 * Garbage Free Allocation.
 */
public class Presentation {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationHelper tools = new PresentationHelper();

	public static void main(String[] args) {
		new Presentation().run();
	}

	void run() {
		runExamples();
		//testCheck();
		//showPrintFlagsFinal();
		//testCheckPool();
		//testAllocationFree();
		//new ShowGcInfo().run();
		//testHeapObserver();
		//testMemoryObserver();
	}

	void runExamples() {
		// Show performance only
		//new PresentationTools().setFastMode(true).setProfileGc(false).runBenchmark(Sample_00_BadBenchmark.class);
		// Show perfomance with GC
		//new PresentationTools().setFastMode(true).runBenchmark(Sample_00_BadBenchmark.class);

		// Show performance only with factor
		//new PresentationTools().setFastMode(true).setProfileGc(false).setFactorRow(true).runBenchmark(Sample_00_BadBenchmark.class);

		//new PresentationTools().runBenchmark(Sample_00_BadBenchmark.class);
		//new PresentationTools().setFactorRow(true).runBenchmark(Sample_00_BadBenchmark.class);

		//opts.setBenchmarkParameterTemplate("^.*\\.(\\w+{class})_(\\w+{method})"); // FIXME

		// Run samples

		//new PresentationHelper().setForce(true).setVerboseBuildJavac(true).runBenchmark(Sample_00_BadBenchmark.class);

		//new PresentationHelper().setVerboseBuildJavac(true).runBenchmark(Sample_01_EnumValues.class);

		new PresentationHelper().setVerboseBuildJavac(true).runBenchmark(Example_02_Autoboxing.class);
		//new PresentationHelper().setVerboseBuildJavac(true).runBenchmark(Example_02_Autoboxing_Bad.class);

		//

		//tools.runMeasureWithGc(Sample_00_BadBenchmark.class);
		// tools.runMeasureWithGc(Check01_Integer.class);

		//tools.runMeasureWithGc(Sample_01_EnumValues.class);
		//tools.runMeasureWithGcAndJava(Sample_01_EnumValues.class);

		// Runtime 1 min: 4 versions * 2 benchmarks * 8s (compile 3s + runtime 5s) = 64 s 
		//tools.runMeasureWithGcAndJava(Sample_02_Autoboxing.class);
		//tools.runMeasureWithGcAndJava(Sample_02_Autoboxing_Bad.class);

		//runMeasureWithGcAndJava(Check02_AutoboxingCaveat.class);
		//tools.runMeasureWithGcAndJava(Sample_03_ReturnNew.class);

		// Run only for Java 8
		//tools.runMeasureWithGc(Sample_03_ReturnNew.class); // Java 8
		//tools.runMeasureWithGcAndJava(Sample_03_ReturnNew_Bad.class);

		//tools.runMeasureWithGcAndJava(Sample_04_IterateCollection.class);
		//runMeasureWithGcAndJava(Check05_Listable.class);

		//tools.runMeasureWithGc(Sample_10_FluentLogging.class);

		//tools.runMeasureWithGcAndFactor(Sample_07_Exception.class);
		//runMeasureWithGcAndFactor(Check07_ExceptionRethrow.class);
		//runMeasureWithGcAndFactor(Check07_ExceptionOmitStackTrace.class);

		//new PresentationTools().setFactorRow(true).runBenchmark(Sample_09_ConcurrentAllocation.class);

		//tools.runMeasureWithGcAndFactor(Sample_09_ConcurrentAllocation.class);

		//runExamplesException();
		//runMeasureWithGcAndJava(Check08_VarArgs.class);
		//runMeasureWithGcAndJava(Check10_Streamer.class);

		//runMeasureWithGcAndJava(TestLoggingFrameworks.CheckLogging.class);		//
		//runMeasureWithGc(CheckStringSplit.class);
		//runMeasureWithGcAndJava(Check_EnumValues.class);
		//		runMeasureWithGcAndJava(Check_ArraysStream.class);
		//runMeasureWithGcAndJava(EvalReplace.class);

		//runMeasureWithGcAndJava(Check_Inlining.class);
		//runInlining();

		//tools.runMeasureWithGc(EvalDateTimeFormatter.class);

		//new EvalFormatService().run();
		//tools.runMeasureWithGc(EvalFormatService.class);

		//tools.runMeasureWithGcAndJava(EvalIntObjGapList.class);

		//tools.runMeasureWithGcAndJava(EvalService.class);
		//tools.runMeasureWithGc(EvalService.class);

		//tools.runMeasureWithGc(CheckServiceOptionsResult.class);
		//tools.runMeasureWithGc(Check_ArraysStream.class);

		//new PresentationTools().setForce(false).runBenchmark(Sample_11_StringBuilder.class);

		//new PresentationTools().setJavaVersions(JavaVersion.JAVA_11).runBenchmark(Sample_06_Listable.class);

		//new PresentationTools().runBenchmark(Sample_14_Service.class);

		//new PresentationTools().setProfileTime(true).setRunTimeMillis(500).runBenchmark(Sample_13_CheckFramework.class);
		// not allocation free with Java 17
		//new PresentationTools().setJavaVersions(JavaVersion.JAVA_17).runBenchmark(Sample_13_CheckFramework.class);
		//new PresentationTools().setJavaVersions(JavaVersion.JAVA_17).setProfileTime(true).setRunTimeMillis(500)

		// 1) testCheckMultiFail:
		// - show in compilation log that method is compiled
		// - with the exception, the method is de-optimized
		// - after some time, it is compiled again
		// - the measurement iteration consumes a lot of memory (even if just a single call fails)

		// 2) testCheckMultiFail:
		// - Add MyJmhState to see what objects are created

		// 
		//		.runBenchmark(Sample_13_CheckFramework.CheckFrameworkTest.class, "testCheckMultiFail");

		//		JitWatchTool jwt = new JitWatchTool();
		//		jwt.setJitLogFile(FilePath.of("hotspot.log"));
		//		jwt.setClassPath("bin/main;build/jmh");
		//		jwt.setSourcePath("src/main/java");
		//		jwt.run();
	}

	static void testCheck() {
		Class<?> clazz = Example_07_ExceptionOmitStackTrace.class;
		Options opts = new Options().includeClass(clazz);
		//opts.setJavacVersion(JavaVersion.JAVA_8);

		// Disable Escape Analysis
		// - checks 1, 2, 5, 8 are also allocation-free without escape analysis starting with Java 17
		//opts.setJvmArgs(GapList.create("-XX:-DoEscapeAnalysis"));
		// Disable Inlining
		// - all checks need allocation
		//opts.setJvmArgs(GapList.create("-XX:-Inline"));

		JmhRunner runner = new JmhRunner();
		opts.setUseGcProfiler(true);
		runner.runJmh(opts);

		//boolean allocFree = JmhRunner.runForAllAllocationFreeResult(opts);
		//LOG.info("All benchmarks in {} are allocation free: {}", clazz.getName(), allocFree);
		//CheckTools.check(allocFree);

		//IList<JavaVersion> jvs = GapList.create(JavaVersion.JAVA_8, JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);
		//boolean allocFree = JmhRunner.runForAllAllocationFreeResult(opts, jvs);
		//LOG.info("All benchmarks in {} are allocation free: {}", clazz.getName(), allocFree);
		//CheckTools.check(allocFree);

		//opts.setJvmArgs(GapList.create("-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"));
	}

	void testHeapObserver() {
		HeapObserver heapObserver = new HeapObserver().setHprofFile(FilePath.of("output/GarbageFreeAllocation.hprof"));
		heapObserver.start();

		alloc();

		heapObserver.stop();
		IList<Tuple<Integer, String>> list = heapObserver.getAllocatedInstances();
		LOG.info("{}", list);
	}

	void testMemoryObserver() {
		// tool for using MemoryHistoryMonitoring
		MemoryReporter mr = new MemoryReporter();

		alloc();

		long used = mr.getUsedMemory();
		LOG.info("{} byte", used);
	}

	void alloc() {
		for (int i = 0; i < 1000; i++) {
			@SuppressWarnings("unused")
			byte[] data = new byte[1024 * 1024];
		}
	}

}
