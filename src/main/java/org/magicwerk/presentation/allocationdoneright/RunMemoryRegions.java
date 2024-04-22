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
 * Show use of Java memory.
 */
public class RunMemoryRegions {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new RunMemoryRegions().run();
	}

	void run() {
		// Show Monitor tab: memory getting allocated and released
		// Show VisualGC tab: memory is allocated in Eden, then moved to Survivor 0/1, but not moved to Old
		//showEdenSurvivor();

		// Show VisualGC tab: memory is allocated in Eden, then moved to Survivor 0/1, then moved to Old
		// Old is clean regularly by GC and does not grow constantly
		//showEdenSurvivorTenured();

		// Show VisualGC tab: memory is direclty allocated in Tenured space due to humongous allocation
		showHumongousTenured();

		// Show VisualGC tab: due to the leak, old generation is growing even if GC is done regularly
		// Profiler tab: Memory settings = org.magicwerk.brownies.tools.**, press "Memory"
		// See growing number of "Allocated Objects" and "Generations"
		// Expand AllocationWorker$Block to see allocation site
		// Note: Use profiler with care in production
		//showEdenSurvivorTenuredWithLeak();

		// Solving Memory Leaks without Heap Dumps (The Old Object Sample Event), needs at least Java 10
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
		//showEdenSurvivorTenuredWithLeakUsingJmc();
	}

	void showEdenSurvivor() {
		runTestAllocator(false, false, false);
	}

	void showEdenSurvivorTenured() {
		runTestAllocator(false, false, true);
	}

	void showHumongousTenured() {
		runTestAllocator(true, false, false);
	}

	void showEdenSurvivorTenuredWithLeak() {
		runTestAllocator(false, true, false);
	}

	void showEdenSurvivorTenuredWithLeakUsingJmc() {
		JavaVersion jv = JavaVersion.JAVA_11;
		JavaTool jt = PresentationHelper.createJavaTool(jv);
		jt.setPrintOutput(true);

		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(jt).setMainMethod(RunMemoryRegions.class, "doShowGcRegionsWithLeakJmc");
		jr.run();
	}

	void doShowGcRegionsWithLeakJmc() {
		showEdenSurvivorTenuredWithLeak();
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
