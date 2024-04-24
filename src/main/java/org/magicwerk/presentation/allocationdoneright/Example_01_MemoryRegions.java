package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.files.ByteUnit;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools.MemoryGcInfo;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools.MemoryInfo;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.dev.workers.AllocationWorker;
import org.magicwerk.brownies.tools.runner.JavaRunner;

import ch.qos.logback.classic.Logger;

/**
 * Show use of Java memory regions.
 */
public class Example_01_MemoryRegions {

	//runEdenSurvivor();
	// Show Monitor tab: memory getting allocated and released
	// Show VisualGC tab: memory is allocated in Eden, then moved to Survivor 0/1, but not moved to Old

	//runEdenSurvivorTenured();
	// Show VisualGC tab: memory is allocated in Eden, then moved to Survivor 0/1, then moved to Old
	// Old is clean regularly by GC and does not grow constantly

	//runHumongousTenured();
	// Show VisualGC tab: memory is directly allocated in Tenured space due to humongous allocation

	//runLeak();
	// - VisualGC
	// Show VisualGC tab: due to the leak, old generation is growing even if GC is done regularly
	// Profiler tab: Memory settings = org.magicwerk.brownies.tools.**, press "Memory"
	// See growing number of "Allocated Objects" and "Generations"
	// Expand AllocationWorker$Block to see allocation site
	// Note: Use profiler with care in production
	// - JFR/JMC
	// Solving Memory Leaks without Heap Dumps (The Old Object Sample Event)
	// https://hirt.se/blog/?p=1055
	// Allocation Profiling in Java Mission Control (The JFR Allocation Event)
	// http://hirt.se/blog/?p=381
	// JMC
	// Open JavaRunner (not MemoryToolsTest)
	//		1st page:
	//			Time fixed recording: 30s
	//			Event settings: profiling - on server
	//	    2nd page: 
	//          Garbage Collection: All (To see TLAB Allocations)
	//			Memory Leak Detection: Object Types + Allocation Stack Traces + Path to GC Root
	///     Finish:
	// Automated Analysis Result:
	//			Heap Live Set Trend
	//			Java Application / Memory / Live Objects: 
	//			Select one, expand AllocationWorker to see stack trace
	// JVM Internals / TLAB Allocations: show that there allocations outside TLAB

	static final Logger LOG = LogbackTools.getConsoleLogger();

	void runEdenSurvivor() {
		run("doRunEdenSurvivor", JavaVersion.JAVA_8);
	}

	void runEdenSurvivorTenured() {
		run("doRunEdenSurvivorTenured", JavaVersion.JAVA_8);
	}

	void runHumongousTenured() {
		run("doRunHumongousTenured", JavaVersion.JAVA_8);
	}

	void runLeak() {
		run("doRunLeak", JavaVersion.JAVA_11);
	}

	void doRunEdenSurvivor() {
		runTestAllocator(false, false, false);
	}

	void doRunEdenSurvivorTenured() {
		runTestAllocator(false, false, true);
	}

	void doRunHumongousTenured() {
		runTestAllocator(true, false, false);
	}

	void doRunLeak() {
		runTestAllocator(false, true, false);
	}

	void run(String method, JavaVersion jv) {
		JavaTool jt = PresentationRunner.createJavaTool(jv);
		jt.setPrintOutput(true);

		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(jt).setMainMethod(Example_01_MemoryRegions.class, method);
		jr.run();
	}

	void runTestAllocator(boolean huge, boolean leak, boolean age) {
		LOG.info("{}", SystemTools.getJavaVersion());
		LOG.info("{}", MemoryTools.getGarbageCollectorInfo());
		LOG.info("{}", MemoryTools.getHeapMemoryUsage());

		MemoryInfo mi = MemoryTools.getMemoryInfo();
		MemoryGcInfo mgi = MemoryTools.getMemoryGcInfo();
		LOG.info("\nSTART\n{}\n{}\n{}\n{}", mi, mgi, MemoryTools.getGcCountInfo(), MemoryTools.getMemoryPoolInfo());

		int numCycles = 1000;
		int sleep = 500;
		int size = (int) ByteUnit.parseValue("100mb");
		int numChunks = (huge) ? 1 : 100;
		int numLeakedChunks = (leak) ? 10 : 0;
		int cycleAge = (age) ? 10 : 0;
		AllocationWorker aw = new AllocationWorker(size).setCycleAge(cycleAge).setNumChunks(numChunks).setNumLeakedChunks(numLeakedChunks);
		for (int i = 0; i < numCycles; i++) {
			LOG.info("Cycle {}/{}", i, numCycles);
			aw.cycle();
			ThreadTools.sleep(sleep);
		}

		mi = MemoryTools.getMemoryInfo();
		mgi = MemoryTools.getMemoryGcInfo();
		LOG.info("\nEND\n{}\n{}\n{}\n{}", mi, mgi, MemoryTools.getGcCountInfo(), MemoryTools.getMemoryPoolInfo());
	}

}
