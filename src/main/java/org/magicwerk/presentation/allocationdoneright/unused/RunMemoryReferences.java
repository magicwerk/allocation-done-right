package org.magicwerk.presentation.allocationdoneright.unused;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.memory.MemoryTools;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.presentation.allocationdoneright.PresentationRunner;

import ch.qos.logback.classic.Logger;

/**
 * Show use of memory references.
 */
public class RunMemoryReferences {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new RunMemoryReferences().run();
	}

	public static final int HOW_MANY = 500_000;

	void run() {
		//doRun();
		JavaRunner jr = new JavaRunner();
		//jr.setJavaTool(JavaTools.createJavaTool(new JavaEnvironment().getSystemJavaVersion()));
		jr.setJavaTool(PresentationRunner.createJavaTool(JavaVersion.JAVA_11));
		jr.setJvmArgs("-verbose:gc");
		jr.setMainMethod(RunMemoryReferences.class, "doRun");
		jr.run();
	}

	/**
	 * Java 8 / Parallel GC:
	 * - SoftReference: refs removed: 0 - many - 0 (runtime: 50, but application does not behave well due to memory issues)
	 * - WeakReference: refs removed: some (runtime: 15 s)
	 * Java 11 / G1 GC:
	 * - SoftReference: refs removed: 0 - many - 0 (runtime: 15 s)
	 * - WeakReference: refs removed: some (runtime: 15 s)
	 */
	void doRun() {
		Class<?> refClass;

		// SoftReference is cleared before a OOME occurs, object may still be alive (but also dead, i.e. null)
		// Tests runs first smoothly, then blocks with heavy GC activity, until some refs are cleared
		//refClass = SoftReference.class;

		// WeakReference, object may still be alive (but also dead, i.e. null)
		// Test runs continuously fine, as references are removed right from the start
		refClass = WeakReference.class;

		// PhantomReference, object is already dead (i.e. null)
		// Test runs like with WeakReference

		doRunRef(refClass);
	}

	void doRunRef(Class<?> refClass) {

		//-XX:+HeapDumpOnOutOfMemoryError -Xmx4096m
		//try with
		// -XX:+UnlockExperimentalVMOptions -XX:G1MaxNewSizePercent=75 -XX:G1NewSizePercent=50 -XX:+UseG1GC
		// or with
		//-XX:+CMSParallelRemarkEnabled, -XX:+UseConcMarkSweepGC, -XX:+UseParNewGC, -XX:ParallelGCThreads=8, -XX:SurvivorRatio=25

		LOG.info("Test of {}\n{}", refClass.getSimpleName(), MemoryTools.getMemoryInfo());

		ReferenceQueue<HeavyList> queue = new ReferenceQueue<>();
		Set<Reference<HeavyList>> references = new HashSet<>();
		ThreadTools.sleep(1000);

		Timer t = new Timer();
		allocationLoop(refClass, queue, references, 20);
		LOG.info("Total time {} ", t.elapsedString());

		LOG.info("Force GC");
		System.gc();
		int removed = removeRefs(queue, references);

		LOG.info("End:\n" + MemoryTools.getMemoryInfo() + "    Refs removed " + removed + "   left " + references.size());
	}

	private static void allocationLoop(Class<?> refClass, ReferenceQueue<HeavyList> queue, Set<Reference<HeavyList>> references, int howManyTimes) {
		HeavyList head = new HeavyList(0, null);
		HeavyList oldTail = head;
		for (int i = 0; i < howManyTimes; i++) {

			HeavyList newTail = allocate(HOW_MANY, oldTail);

			HeavyList curr = oldTail.next;
			while (curr != null) {
				Reference<HeavyList> reference;
				if (refClass == SoftReference.class) {
					reference = new SoftReference<>(curr, queue);
				} else if (refClass == WeakReference.class) {
					reference = new WeakReference<>(curr, queue);
				} else if (refClass == PhantomReference.class) {
					reference = new PhantomReference<>(curr, queue);
				} else {
					throw new AssertionError();
				}
				references.add(reference);
				curr = curr.getNext();
			}

			deallocateHalf(head);

			int removed = removeRefs(queue, references);

			//  System.gc();   //uncomment this line to comparing with forced gc
			LOG.info(MemoryTools.getMemoryInfo() + "    Refs removed " + removed + "   left " + references.size());

			oldTail = newTail;
		}
		head = null;
		oldTail = null;
	}

	private static int removeRefs(ReferenceQueue queue, Set<Reference<HeavyList>> references) {
		int removed = 0;
		while (true) {
			Reference r = queue.poll();
			if (r == null)
				break;
			references.remove(r);
			removed++;
		}
		return removed;
	}

	private static void deallocateHalf(HeavyList head) {
		HeavyList curr = head;

		while (curr != null) {
			curr.dropNext();
			curr = curr.getNext();
		}
	}

	private static HeavyList allocate(int howMany, HeavyList startFrom) {

		HeavyList curr = startFrom;
		for (int i = 0; i < howMany; i++) {
			curr = new HeavyList(i, curr);
		}
		return curr;

	}

	private static int count(HeavyList list) {

		HeavyList curr = list;
		int tot = 0;
		while (curr != null) {
			tot++;
			curr = curr.getNext();
		}
		return tot;

	}

	private static class HeavyList {

		byte[] mega = new byte[1000];
		private HeavyList next = null;

		public HeavyList(int number, HeavyList prev) {
			for (int i = 0; i < mega.length; i++) {
				mega[i] = (byte) (number % 256);
			}
			if (prev != null) {
				prev.next = this;
			}
		}

		public HeavyList getNext() {
			return next;
		}

		public HeavyList dropNext() {
			if (next == null || next.next == null) {
				return null;
			}
			HeavyList res = next;
			next = next.next;
			return res;
		}
	}

}
