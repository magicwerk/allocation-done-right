package org.magicwerk.presentation.allocationdoneright.misc;

import java.util.Map;
import java.util.regex.Pattern;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.exec.Exec.ExecStatus;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.regex.RegexTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.jvm.BlackHole;
import org.magicwerk.brownies.tools.dev.jvm.HeapDumpTools;
import org.magicwerk.brownies.tools.dev.memory.GarbageCollectorMonitoring;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.presentation.allocationdoneright.PresentationHelper;
import org.netbeans.lib.profiler.heap.Heap;

import ch.qos.logback.classic.Logger;

/**
 * Show use of EpsilonGC and heap dumps.
 */
public class RunHeapDump {

	static class Live {
	}

	static class Temp {
	}

	static final Logger LOG = LogbackTools.getConsoleLogger();

	IList<Live> liveList = GapList.create();

	public static void main(String[] args) {
		new RunHeapDump().run();
	}

	void run() {
		FilePath hprofFile = runUntilHeapDumpOnOOME();
		//analyzeHprofFile(FilePath.of("temp.hprof"));
		analyzeHprofFile(hprofFile);

		// File cannot be deleted as it is locked. Also an additional directory is created, e.g. java_pid60548.hprof.nbcache
		//FileTools.deleteFile().setFile(hprofFile).delete();
	}

	void analyzeHprofFile(FilePath hprofFile) {
		Heap hd = HeapDumpTools.readHeapDump(hprofFile);
		Map<String, Integer> ccm = HeapDumpTools.getClassCountMap(hd);
		ccm.keySet().removeIf(s -> !s.startsWith(RunHeapDump.class.getName() + "$"));
		LOG.info("{}", ccm);
	}

	/**
	 * Run until an OOME occurs and return path to the heap dump file.
	 */
	FilePath runUntilHeapDumpOnOOME() {
		JavaVersion jv = JavaVersion.JAVA_21;
		JavaTool jt = PresentationHelper.createJavaTool(jv);
		jt.setPrintOutput(true);

		JavaRunner jr = new JavaRunner();

		// EpsilonGC does not GC - there will be as many Temp as Live objects
		//jr.setJvmArgs(CollectionTools.concat(JavaTool.JvmUseEpsilonGc, JavaTool.JvmHeapDumpOnOOME, "-Xmx48m"));

		// Few GC will occur before OOME, -the heapdump will contain a of Live, but only one Temp object
		jr.setJvmArgs(CollectionTools.concat(JavaTool.JvmHeapDumpOnOOME, "-Xmx48m"));

		jr.setJavaTool(jt).setMainMethod(RunHeapDump.class, "doRunHeapDumpOnOOME");
		ExecStatus status = jr.run();
		String msg = status.getMessage();
		String file = RegexTools.get(HPROF_FILE, msg);
		return FilePath.of(file);
	}

	// Dumping heap to java_pid10256.hprof ...
	static final Pattern HPROF_FILE = Pattern.compile("(?m)^Dumping heap to (\\w+\\.hprof)");

	void doRunHeapDumpOnOOME() {
		GarbageCollectorMonitoring gcm = GarbageCollectorMonitoring.forLogging();
		gcm.start();

		int sleep = 500;
		int steps = 1000;
		for (int i = 0; i < 1000; i++) {
			LOG.info("Cycle {}/{}", i, steps);
			LOG.info("{}", MemoryTools.getHeapMemoryUsage());

			for (int j = 0; j < 100_000; j++) {
				Temp temp = new Temp();
				BlackHole.consume(temp);
				Live live = new Live();
				liveList.add(live);
			}

			ThreadTools.sleep(sleep);

			if (i == 10) {
				//HeapDumpTools.createHeapDump(FilePath.of("temp.hprof"), true, true);
			}
		}

		gcm.stop();
	}

}
