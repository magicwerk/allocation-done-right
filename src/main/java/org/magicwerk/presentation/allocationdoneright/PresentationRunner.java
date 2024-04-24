package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.FuncTools;
import org.magicwerk.brownies.core.FuncTools.MapMode;
import org.magicwerk.brownies.core.collections.PivotTableCreator;
import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.streams.CaptureSystemOutput;
import org.magicwerk.brownies.core.streams.CaptureSystemOutput.CaptureMode;
import org.magicwerk.brownies.core.strings.text.TextTools;
import org.magicwerk.brownies.core.validator.DecimalFormatter;
import org.magicwerk.brownies.core.validator.NumberFormatter;
import org.magicwerk.brownies.core.validator.PercentFormatter;
import org.magicwerk.brownies.core.validator.PercentFormatter.PercentType;
import org.magicwerk.brownies.html.ReportTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.jvm.JavaEnvironment;
import org.magicwerk.brownies.tools.dev.jvm.JdkCommand;
import org.magicwerk.brownies.tools.dev.jvm.JdkCommands;
import org.magicwerk.brownies.tools.dev.jvm.JmhReporter;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.BenchmarkResult;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.BenchmarkResult.Metric;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;

import ch.qos.logback.classic.Logger;

/**
 * Helpers for Garbage Free Allocation.
 */
public class PresentationRunner {

	static final Logger LOG = LogbackTools.getLogger();

	FilePath file = FilePath.of("output/result.json");

	static final JavaVersion defaultJavaVersion = JavaVersion.JAVA_21;
	static final IList<JavaVersion> allJavaVersions = GapList.create(JavaVersion.JAVA_8, JavaVersion.JAVA_11, JavaVersion.JAVA_17, JavaVersion.JAVA_21);

	JavaTool defaultJavaTool = createJavaTool(defaultJavaVersion);
	JdkCommands defaultJdkTools = createJdkCommands(defaultJavaVersion);
	IList<JdkCommands> allJdkTools = GapList.create(
			createJdkCommands(JavaVersion.JAVA_8),
			createJdkCommands(JavaVersion.JAVA_11),
			createJdkCommands(JavaVersion.JAVA_17),
			createJdkCommands(JavaVersion.JAVA_21));

	{
		allJdkTools = GapList.create(
				createJdkCommands(JavaVersion.JAVA_8),
				createJdkCommands(JavaVersion.JAVA_11),
				createJdkCommands(JavaVersion.JAVA_17),
				createJdkCommands(JavaVersion.JAVA_21));
	}

	IList<JdkCommands> createJdkTools(IList<JavaVersion> javaVersions, IList<JavaVersion> javacVersions) {
		// Check arguments
		int numJv = javaVersions.size();
		int numJcv = javacVersions.size();
		CheckTools.check(numJv > 0 || numJcv > 0);
		if (numJv > 1) {
			CheckTools.check(numJcv == numJv || numJcv <= 1);
		}
		if (numJcv > 1) {
			CheckTools.check(numJv == numJcv || numJv <= 1);
		}

		// Create tools
		int size = Math.max(numJv, numJcv);
		IList<JdkCommands> jts = new GapList<>(size);
		for (int i = 0; i < size; i++) {
			JavaVersion jv = FuncTools.map(numJv, MapMode.DEFAULT, 0, null, 1, CollectionTools.get(javaVersions, 0), CollectionTools.get(javaVersions, i));
			JavaVersion jcv = FuncTools.map(numJcv, MapMode.DEFAULT, 0, null, 1, CollectionTools.get(javacVersions, 0), CollectionTools.get(javacVersions, i));
			JdkCommand jtj = createJavaCommand(jv);
			JdkCommand jtjc = createJavacCommand(jcv);
			JdkCommands jt = new JdkCommands().setJavaCommand(jtj).setJavacCommand(jtjc);
			jts.add(jt);
		}
		return jts;
	}

	/** Create {@link JavaTool} for specified version */
	public static JavaTool createJavaTool(JavaVersion jv) {
		if (jv == null) {
			jv = JavaEnvironment.getSystemJavaVersion();
		}
		JavaTool jt = new JavaTool();
		jt.setExecutable(JavaEnvironment.getJavaExe(jv));
		return jt;
	}

	public static JdkCommand createJavaCommand(JavaVersion jv) {
		JdkCommand javaTool = new JdkCommand();
		javaTool.setExecutable(JavaEnvironment.getJavaExe(jv));
		javaTool.setVersion(jv.getName());

		if (jv == JavaVersion.JAVA_17 || jv == JavaVersion.JAVA_21) {
			javaTool.setArgs(GapList.create(
					"--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED"));
		}

		return javaTool;
	}

	public static JdkCommand createJavacCommand(JavaVersion jv) {
		JdkCommand javacTool = new JdkCommand();
		javacTool.setExecutable(JavaEnvironment.getJavacExe(jv));
		javacTool.setVersion(jv.getName());
		return javacTool;
	}

	public static JdkCommands createJdkCommands(JavaVersion jv) {
		JdkCommands jdkTools = new JdkCommands();
		jdkTools.setJavaCommand(createJavaCommand(jv));
		jdkTools.setJavacCommand(createJavacCommand(jv));
		return jdkTools;
	}

	//

	IList<JdkCommands> jdkEnvs;
	boolean fastMode;
	boolean profileGc = true;
	boolean profileTime;
	int runTimeMillis = 1000;
	boolean verboseBuildJavac = true;
	boolean force;
	FilePath outputDir = FilePath.of("output");

	PresentationRunner setVerboseBuildJavac(boolean verboseBuildJavac) {
		this.verboseBuildJavac = verboseBuildJavac;
		return this;
	}

	PresentationRunner setForceRun(boolean force) {
		this.force = force;
		return this;
	}

	PresentationRunner setFastMode(boolean fastMode) {
		this.fastMode = fastMode;
		return this;
	}

	PresentationRunner setProfileGc(boolean profileGc) {
		this.profileGc = profileGc;
		return this;
	}

	PresentationRunner setProfileTime(boolean profileTime) {
		this.profileTime = profileTime;
		return this;
	}

	public PresentationRunner setRunTimeMillis(int runTimeMillis) {
		this.runTimeMillis = runTimeMillis;
		return this;
	}

	public PresentationRunner setJavaVersions(IList<JavaVersion> javaVersions) {
		this.jdkEnvs = javaVersions.map(PresentationRunner::createJdkCommands);
		return this;
	}

	public PresentationRunner setJavaVersions(JavaVersion... javaVersions) {
		this.jdkEnvs = GapList.create(javaVersions).map(PresentationRunner::createJdkCommands);
		return this;
	}

	void runBenchmark(Class<?> clazz) {
		runBenchmark(clazz, null);
	}

	void runBenchmark(Class<?> clazz, String method) {
		//force = true; // FIXME

		String name = clazz.getName();
		if (method != null) {
			name += "." + method;
		}

		FileTools.cleanDir().setDir(outputDir).create();
		FilePath logFile = outputDir.get(name + ".log");
		FilePath htmlFile = outputDir.get(name + ".html");

		String text = null;
		if (!force) {
			text = FileTools.readFile().setFile(logFile).setIgnoreErrors(true).readText();
		}
		if (text != null) {
			printLog(text);
			if (FileTools.isFile(htmlFile)) {
				ReportTools.showHtmlFile(htmlFile.getPath());
			}
			return;
		}

		CaptureSystemOutput.startCapture(CaptureMode.SystemOutErr);
		doRunBenchmark(htmlFile, clazz, method);
		String out = CaptureSystemOutput.stopCapture();

		FileTools.writeFile().setFile(logFile).setText(out).write();
	}

	void printLog(String text) {
		IList<String> lines = TextTools.splitLines(text);
		for (String line : lines) {
			LOG.info(line);
			ThreadTools.sleep(1);
		}
	}

	void doRunBenchmark(FilePath htmlFile, Class<?> clazz, String method) {
		Options opts = createOptions(clazz, method);
		if (jdkEnvs != null) {
			opts.setJavaVersions(jdkEnvs);
		} else {
			opts.setJavaVersions(GapList.create(defaultJdkTools));
		}
		opts.setUseGcProfiler(profileGc);
		if (profileTime) {
			opts.setWarmupIterations(5).setMeasurementIterations(5);
		} else {
			opts.setWarmupIterations(3).setMeasurementIterations(2);
		}
		opts.setRunTimeMillis(runTimeMillis);

		JmhRunner runner = new JmhRunner();
		runner.setFastMode(fastMode);
		runner.setVerboseBuildJavac(verboseBuildJavac);
		runner.runJmh(opts);

		JmhReporter jr = new JmhReporter();
		jr.setHtmlFile(htmlFile);
		jr.setShowUnit(false);
		if (profileGc) {
			jr.setShowMetrics(GapList.create(Metric.Metric_Performance, BenchmarkResult.KEY_gcAllocRateNorm));
		} else {
			jr.setShowMetrics(GapList.create(Metric.Metric_Performance));
		}
		jr.setFiles(GapList.create(file));

		PivotTableCreator ptc = new PivotTableCreator()
				.setRowKeys(GapList.create(JmhReporter.COL_Benchmark))
				.setCellKeys(GapList.create(JmhReporter.COL_Score))
				.setValueFormatter(new DecimalFormatter<Number>("#,###"));

		IList<String> colKeys = GapList.create();
		if (jdkEnvs != null && jdkEnvs.size() > 1) {
			colKeys.add("java");
		}
		if (profileGc) {
			colKeys.add(JmhReporter.COL_Metric);
		}
		if (!colKeys.isEmpty()) {
			ptc.setColKeys(colKeys);
		}

		jr.showPivotTable(ptc);
		LOG.info("{}", jr.printPivotTable(ptc));
	}

	//

	void runMeasureWithGcAndFactor(Class<?> clazz) {
		Options opts = createOptions(clazz, null);
		opts.setJavaVersions(GapList.create(defaultJdkTools));
		opts.setUseGcProfiler(true);
		opts.setWarmupIterations(3).setMeasurementIterations(2);

		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);

		JmhReporter jr = new JmhReporter();
		jr.setShowUnit(false);
		jr.setShowMetrics(GapList.create(Metric.Metric_Performance, BenchmarkResult.KEY_gcAllocRateNorm));
		jr.setFiles(GapList.create(file));

		PivotTableCreator ptc = new PivotTableCreator()
				.setColKeys(GapList.create(JmhReporter.COL_Metric))
				.setRowKeys(GapList.create(JmhReporter.COL_Benchmark))
				.setCellKeys(GapList.create(JmhReporter.COL_Score))
				.setValueFormatter(new NumberFormatter<Number>(0))
				.setAddFactorRow(true)
				.setFactorFormatter(new PercentFormatter<Number>(PercentType.Percent, false, 1));
		jr.showPivotTable(ptc);
		LOG.info("{}", jr.printPivotTable(ptc));
	}

	void runMeasureWithGcAndJava(Class<?> clazz) {
		Options opts = createOptions(clazz, null);
		opts.setJavaVersions(allJdkTools);
		opts.setUseGcProfiler(true);
		opts.setWarmupIterations(3).setMeasurementIterations(2);
		//opts.setWarmupIterations(5).setMeasurementIterations(5);

		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);

		JmhReporter jr = new JmhReporter();
		jr.setShowUnit(false);
		jr.setShowMetrics(GapList.create(Metric.Metric_Performance, BenchmarkResult.KEY_gcAllocRateNorm));
		jr.setFiles(GapList.create(file));

		PivotTableCreator ptc = new PivotTableCreator()
				.setColKeys(GapList.create("java", JmhReporter.COL_Metric))
				.setRowKeys(GapList.create(JmhReporter.COL_Benchmark))
				.setCellKeys(GapList.create(JmhReporter.COL_Score))
				.setValueFormatter(new NumberFormatter<Number>(0));
		jr.showPivotTable(ptc);
		LOG.info("{}", jr.printPivotTable(ptc));
	}

	void runMeasureWithGcAndJava2(Class<?> clazz) {
		Options opts = createOptions(clazz, null);
		opts.setJavaVersions(allJdkTools);
		opts.setUseGcProfiler(true);
		opts.setWarmupIterations(3).setMeasurementIterations(2);
		//opts.setWarmupIterations(5).setMeasurementIterations(5);

		JmhRunner runner = new JmhRunner();
		//runner.runJmh(opts);

		JmhReporter jr = new JmhReporter();
		jr.setShowUnit(false);
		jr.setShowMetrics(GapList.create(Metric.Metric_Performance, BenchmarkResult.KEY_gcAllocRateNorm));
		jr.setFiles(GapList.create(file));

		PivotTableCreator ptc = new PivotTableCreator()
				.setColKeys(GapList.create("java", JmhReporter.COL_Metric))
				.setRowKeys(GapList.create(JmhReporter.COL_Benchmark, "logLevel"))
				.setCellKeys(GapList.create(JmhReporter.COL_Score))
				.setValueFormatter(new NumberFormatter<Number>(0));
		jr.showPivotTable(ptc);
		LOG.info("{}", jr.printPivotTable(ptc));
	}

	Options createOptions(Class<?> clazz, String method) {
		Options opts = new Options();
		if (method != null) {
			opts.includeMethod(clazz, method);
		} else {
			opts.includeClass(clazz);
		}
		opts.setSourceDir("src/main/java");
		opts.setResultFile(file);
		return opts;
	}

}
