package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.core.logback.LogbackTools;

import ch.qos.logback.classic.Logger;

/**
 * Presentation "Allocation Done Right".
 */
public class Presentation {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationRunner tools = new PresentationRunner();

	public static void main(String[] args) {
		new Presentation().run();
	}

	void run() {
		// - Benchmark First!
		new PresentationRunner().setForceRun(true).runBenchmark(Example_00_BadBenchmark.class);

		// - (Simple) Java Memory Model
		// SKIP: new Example_01_MemoryRegions().runEdenSurvivorTenured();
		// SKIP: new Example_01_MemoryRegions().runEdenSurvivor();
		// SKIP: new Example_01_MemoryRegions().runHumongousTenured();

		// - A Memory Leak}
		// org.magicwerk.brownies.tools.**
		//new Example_01_MemoryRegions().runLeak();

		// - Memory and Performance
		//new PresentationRunner().runBenchmark(Example_02_ConcurrentAllocation.class);

		// - Collection and Data Structures
		// SKIP: new Example_10_CollectionSize().run();
		// SKIP: new Example_11_StringBuilder().run();
		//new PresentationRunner().runBenchmark(Example_11_StringBuilder.class);
		// SKIP: eval.java21.EvalScopedValue

		// SKIP: Example_12_Inlining

		//new PresentationRunner().runBenchmark(Example_18_ReturnNew.class);

		// Example_19_AllocationFreeTest

		//new PresentationRunner().setJavaVersions(PresentationRunner.allJavaVersions).runBenchmark(Example_20_Autoboxing.class);
		//new PresentationRunner().setJavaVersions(PresentationRunner.allJavaVersions).runBenchmark(Example_21_VarArgs.class);
		//new PresentationRunner().setJavaVersions(PresentationRunner.allJavaVersions).runBenchmark(Example_22_EnumValues.class);

		//new PresentationRunner().setJavaVersions(JavaVersion.JAVA_11).runBenchmark(Example_23_Exception.class);

		//new PresentationRunner().setJavaVersions(PresentationRunner.allJavaVersions).runBenchmark(Example_24_IterateCollection.class);

		//new PresentationRunner().runBenchmark(Example_30_StatelessService.class);

		//new PresentationRunner().runBenchmark(Example_31_FluentLogging.class);

		//new PresentationRunner().setJavaVersions(PresentationRunner.allJavaVersions).runBenchmark(Example_32_CheckFramework.CheckFrameworkTest.class);
		//new PresentationRunner().runBenchmark(Example_32_CheckFramework.CheckFrameworkTestExample.class);
	}

}
