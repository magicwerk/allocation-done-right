package org.magicwerk.presentation.allocationdoneright.unused;

import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.presentation.allocationdoneright.PresentationRunner;

import ch.qos.logback.classic.Logger;

/**
 * Show use of EpsilonGC and heap dumps.
 */
public class ShowJvmFlags {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ShowJvmFlags().run();
	}

	void run() {
		JavaTool jt = PresentationRunner.createJavaTool(JavaVersion.JAVA_8);
		jt.setPrintOutput(true);

		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(jt);

		{
			jr.setJvmArgs("-XX:+PrintFlagsFinal", "-showversion");
			jr.setMainMethod(ShowJvmFlags.class, "runVoid");
			jr.run();
		}
		//		{
		//			// -version prints out version information and terminates JVM
		//			jr.setJvmArgs("-version");
		//			jr.setMainMethod(ShowJvmFlags.class, "runVoid");
		//			jr.run();
		//		}
		//		{
		//			// -showversion prints out version information but continues running
		//			jr.setJvmArgs("-showversion");
		//			jr.setMainMethod(ShowJvmFlags.class, "runVoid");
		//			jr.run();
		//		}

		// https://www.codecentric.de/wissens-hub/blog/useful-jvm-flags-part-3-printing-all-xx-flags-and-their-values
		//jr.setJvmArgs("-XX:+PrintCommandLineFlags", "-version");
		//jr.setJvmArgs("-XX:+PrintFlagsInitial", "-version");

		// Java 11: 645 flags
		//jr.setJvmArgs("-XX:+PrintFlagsFinal", "-version");
		// Java 11: 801 flags
		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintFlagsFinal", "-version");
		// Java 11: 720 flags
		//jr.setJvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+PrintFlagsFinal", "-version");
		// Java 11: 876 flags
		//jr.setJvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintFlagsFinal", "-version");
		//jr.run();
	}

	void runVoid() {
		LOG.info("runVoid");
	}

}
