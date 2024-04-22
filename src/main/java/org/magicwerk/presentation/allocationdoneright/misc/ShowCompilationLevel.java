package org.magicwerk.presentation.allocationdoneright.misc;

import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.jvm.JdkCommands;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.presentation.allocationdoneright.PresentationHelper;

import ch.qos.logback.classic.Logger;

/**
 * Show compilation levels and deoptimization.
 * https://theboreddev.com/java-jit-compiler-explained-part-1/
 * OSR (On Stack Replacement):
 * https://web.archive.org/web/20120221015612/http://java.sun.com/developer/technicalArticles/Networking/HotSpot/onstack.html
 */
public class ShowCompilationLevel {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationHelper tools = new PresentationHelper();

	JavaVersion javaVersion = JavaVersion.JAVA_21;
	JdkCommands jdkTools = PresentationHelper.createJdkTools(javaVersion);
	JavaTool javaTool = PresentationHelper.createJavaTool(javaVersion);

	public static void main(String[] args) {
		new ShowCompilationLevel().run();
	}

	void run() {
		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(javaTool);

		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+PrintInlining");
		jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation");
		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining");

		jr.setMainMethod(ShowCompilationLevel.class, "doRun");
		jr.run();
	}

	void doRun() {
		showOnStackReplacement();
	}

	void showOnStackReplacement() {
		int sum = 0;
		for (int index = 0; index < 10 * 1000 * 1000; index += 1) {
			sum += index;
		}
		System.out.println(sum);
	}

	void showDeoptimization() {
		Printer p;
		for (int i = 0; i < 20_000; i++) {
			if (i < 10_000) {
				p = new Printer1();
			} else {
				if (i == 10_000) {
					System.out.println("SWITCH");
				}
				p = new Printer2();
			}
			p.print("Number " + i + " using ");
		}
	}

	interface Printer {
		void print(String msg);
	}

	static class Printer1 implements Printer {

		@Override
		public void print(String msg) {
			ThreadTools.sleep(1);
		}
	}

	static class Printer2 implements Printer {

		@Override
		public void print(String msg) {
			ThreadTools.sleep(1);
		}

	}

}
