package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.jvm.JdkCommands;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;

import ch.qos.logback.classic.Logger;

/**
 * Show how JIT handles inlining and the performance impact it has.
 */
public class Example_03_Inlining {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationHelper tools = new PresentationHelper();

	JavaVersion javaVersion = JavaVersion.JAVA_21;
	JdkCommands jdkTools = PresentationHelper.createJdkTools(javaVersion);
	JavaTool javaTool = PresentationHelper.createJavaTool(javaVersion);

	int inliningStep = 3;

	public static void main(String[] args) {
		new Example_03_Inlining().run();
	}

	void run() {
		// inliningStep:
		// 0 = small method - called seldom (not hot)
		// 1 = small method - called often (hot)
		// 2 = large method - called often (hot, but too large)
		// 3 = large method split - called often (hot)
		runInlining();

		//runJmh();
	}

	void runJmh() {
		// Show performance impact of inlining
		// callLargeMethod                   994'968                      
		// callLargeMethodSplitted           44'722'171                   
		// callLargeMethodSplittedInlineSize 583'563                      

		//tools.runMeasure(Check_Inlining.class);

		// Show how escape analysis is dependent on inlining
		//		callLargeMethod                   1'013'476   0                 
		//		callLargeMethodSplitted           47'968'085  0                 
		//		callLargeMethodSplittedWithObject 45'995'116  0                 
		//		callLargeMethodWithObject         1'121'477   16        
		tools.runMeasureWithGc(Check_Inlining.class);
	}

	void runInlining() {
		JavaRunner jr = new JavaRunner();
		jr.setJavaTool(javaTool);

		//		-XX:+PrintCompilation
		//	     42   44       3       java.util.ImmutableCollections$AbstractImmutableSet::<init> (5 bytes)
		//	     42   53     n 0       jdk.internal.misc.Unsafe::compareAndSetLong (native)   
		//	     42   54   !   3       java.util.concurrent.ConcurrentHashMap::putVal (432 bytes)

		//		-XX:+PrintInlining
		//          @ 5   java.lang.String::<init> (7 bytes)   inline
		//            @ 3   java.lang.String::<init> (99 bytes)   callee is too large
		//        @ 153   java.lang.IllegalArgumentException::<init> (6 bytes)   don't inline Throwable constructors

		//		-XX:+PrintAssembly
		//		============================= C1-compiled nmethod ==============================
		//		----------------------------------- Assembly -----------------------------------
		//		[0.026s][warning][os] Loading hsdis library failed
		//		Compiled method (c1)      25    4       3       jdk.internal.util.ArraysSupport::signedHashCode (37 bytes)

		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation");
		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining");		
		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintAssembly");

		jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+PrintInlining");
		//jr.setJvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+LogCompilation", "-XX:LogFile=hotspot.log");

		jr.setMainMethod(Example_03_Inlining.class, "doRunInlining");
		jr.run();
	}

	void doRunInlining() {
		int SLEEP = 2000;
		int SELDOM = 10;
		int OFTEN = 10_000;

		ThreadTools.sleep(SLEEP);
		System.out.println("=== START ===");
		ThreadTools.sleep(SLEEP);

		// https://wiki.openjdk.org/display/HotSpot/PerformanceTechniques
		Check_Inlining check = new Check_Inlining();

		if (inliningStep == 0) {
			// If the small method is just executed a few time, it will not be compiled, so nothing will happen here
			System.out.println("===========");
			System.out.println("smallMethod a few times");
			for (int i = 0; i < SELDOM; i++) {
				check.smallMethod();
			}
			ThreadTools.sleep(SLEEP);
			System.out.println("===========");
		}

		if (inliningStep == 1) {
			// If the small method is just executed often, it will be compiled:
			// org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::doSmallMethod (25 bytes)   inline (hot)
			System.out.println("===========");
			System.out.println("smallMethod often");
			for (int i = 0; i < OFTEN; i++) {
				check.smallMethod();
			}
			ThreadTools.sleep(SLEEP);
			System.out.println("===========");
		}

		if (inliningStep == 2) {
			// If a large mall method is executed often, it could be compiled, but it is too large:
			// org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::doLargeMethod (377 bytes)   hot method too big

			System.out.println("===========");
			System.out.println("largeMethod often");
			for (int i = 0; i < OFTEN; i++) {
				check.callLargeMethod();
			}
			ThreadTools.sleep(SLEEP);
			System.out.println("===========");
		}

		if (inliningStep == 3) {
			//			// If the large method body is split in several methods, it can be compiled again:
			//			// org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::doLargeMethod (377 bytes)   hot method too big
			//            @ 1   org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::doLargeMethodSplit (28 bytes)   inline (hot)
			//            @ 4   org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::largeMethod3a (115 bytes)   inline (hot)
			//            @ 11   org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::largeMethod3b (115 bytes)   inline (hot)
			//            @ 18   org.magicwerk.brownies.sandbox.presentation.ShowInlining$Check_Inlining::largeMethod3c (152 bytes)   already compiled into a big method
			// https://wiki.openjdk.org/display/HotSpot/Server+Compiler+Inlining+Messages
			System.out.println("===========");
			System.out.println("largeMethodSplit often");
			for (int i = 0; i < OFTEN; i++) {
				check.callLargeMethodSplitted();
			}
			ThreadTools.sleep(SLEEP);
			System.out.println("===========");
		}
	}

	public static class Check_Inlining {

		public int callSmallMethod() {
			Integer i = smallMethod();
			return i.intValue();
		}

		Integer smallMethod() {
			int sum = 0;
			for (int i = 0; i < 10; i++) {
				sum += i;
			}
			return sum;
		}

		static final int ITER1 = 1000;
		static final int ITER2 = 1;

		//@Fork(jvmArgs = { "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation", "-XX:+PrintInlining" })
		@Benchmark
		public int callLargeMethod() {
			int sum = 0;
			for (int i = 0; i < ITER1; i++) {
				sum += largeMethod(sum, null);
			}
			return sum;
		}

		@Benchmark
		public int callLargeMethodSplitted() {
			int sum = 0;
			for (int i = 0; i < ITER1; i++) {
				sum += largeMethodSplitted(sum, null);
			}
			return sum;
		}

		//@Fork(jvmArgs = { "-XX:-Inline" })
		@Fork(jvmArgs = { "-XX:FreqInlineSize=100" })
		@Benchmark
		public int callLargeMethodSplittedInlineSize() {
			int sum = 0;
			for (int i = 0; i < ITER1; i++) {
				sum += largeMethodSplitted(sum, null);
			}
			return sum;
		}

		//@Benchmark
		public int callLargeMethodWithObject() {
			int sum = 0;
			Object o = new Object();
			for (int i = 0; i < ITER1; i++) {
				sum += largeMethod(sum, o);
			}
			return sum;
		}

		//@Benchmark
		public int callLargeMethodSplittedWithObject() {
			int sum = 0;
			Object o = new Object();
			for (int i = 0; i < ITER1; i++) {
				sum += largeMethodSplitted(sum, o);
			}
			return sum;
		}

		int largeMethod(int sum, Object o) {
			// Part 1
			for (int i = 0; i < ITER2; i++) {
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
			}
			// Part 2
			for (int i = 0; i < ITER2; i++) {
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
			}
			return sum;
		}

		int largeMethodSplitted(int sum, Object o) {
			sum += largeMethod3a(sum);
			sum += largeMethod3b(sum);
			return sum;
		}

		int largeMethod3a(int sum) {
			// Part 1
			for (int i = 0; i < ITER2; i++) {
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
			}
			return sum;
		}

		int largeMethod3b(int sum) {
			// Part 1
			for (int i = 0; i < ITER2; i++) {
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
				sum += i * i * i * i * i * i * i * i * i * i;
			}
			return sum;
		}

	}

}
