package org.magicwerk.presentation.allocationdoneright.misc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.javassist.JavaVersion;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;
import org.magicwerk.brownies.tools.runner.JavaRunner;
import org.magicwerk.presentation.allocationdoneright.PresentationHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import ch.qos.logback.classic.Logger;

/**
 * Garbage Free Allocation.
 */
public class TestPoolAllocators {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final PresentationHelper tools = new PresentationHelper();

	public static void main(String[] args) {
		new TestPoolAllocators().run();
	}

	void run() {
		if (false) {
			new CheckPool().runManual();
		}

		if (false) {
			JavaTool jt = PresentationHelper.createJavaTool(JavaVersion.JAVA_11);
			jt.setPrintOutput(true);

			JavaRunner jr = new JavaRunner();
			jr.setMainMethod(CheckPool.class, "runManual");
			jr.setJavaTool(jt);
			jr.setJvmArgs(CollectionTools.concat(JavaTool.JvmUseGcEpsilon, "-Xmx256m"));
			jr.run();
		}

		if (true) {
			//tools.runMeasureWithGcAndJava(CheckPool.class);
			//			Options opts = new Options().includeClass(CheckPool.class);
			//
			//			//opts.setJavaVersion(JavaVersion.JAVA_8);
			//			opts.setJavaVersions(GapList.create(TestTools.createJdkTools(JavaVersion.JAVA_11)));
			//			//opts.setJavaVersion(JavaVersion.JAVA_17);
			//			//opts.setJavaVersion(JavaVersion.JAVA_21);
			//
			//			//opts.setJvmArgs(GapList.create("-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC", "-Xmx512m"));
			//
			//			JmhRunner runner = new JmhRunner();
			//			opts.setUseGcProfiler(true);
			//			runner.runJmh(opts);
		}
	}

	@Threads(Threads.MAX)
	public static class CheckPool {

		void runManual() {
			MyState state = new MyState();
			for (int i = 0; i < 10000; i++) {
				//MemoryReporter mr = new MemoryReporter();

				for (int j = 0; j < 10000; j++) {
					testNewAllocator(state);
					//testPoolAllocator(state);
				}

				//long used = mr.getUsedMemory();
				//LOG.info("{} byte", used);
			}

		}

		@State(Scope.Benchmark)
		public static class MyState {
			NewAllocator newAllocator = new NewAllocator();
			ThreadLocalAllocator threadLocalAllocator = new ThreadLocalAllocator();
			ConcurrentMapAllocator concurrentMapAllocator = new ConcurrentMapAllocator();
			PoolAllocator poolAllocator = new PoolAllocator();
		}

		@Benchmark
		public int testNewAllocator(MyState state) {
			return work(state.newAllocator);
		}

		@Benchmark
		public int testThreadLocalAllocator(MyState state) {
			return work(state.threadLocalAllocator);
		}

		@Benchmark
		public int testConcurrentMapAllocator(MyState state) {
			return work(state.concurrentMapAllocator);
		}

		@Benchmark
		public int testPoolAllocator(MyState state) {
			return work(state.poolAllocator);
		}

		static <T> int work(Allocator<Buffer> allocator) {
			Buffer buf = allocator.get();
			work(buf);
			return buf.size();
		}

		static int work(AllocatorWithRelease<Buffer> allocator) {
			Buffer buf = allocator.get();
			work(buf);
			int count = buf.size();
			allocator.release(buf);
			return count;
		}

		static void work(Buffer buf) {
			for (int i = 0; i < 1000; i++) {
				buf.add((byte) i);
			}
		}
	}

	//

	/** Interface {@link Allocator} supports allocation of object */
	interface Allocator<T> {
		T get();
	}

	/** Interface {@link AllocatorWithRelease} supports allocation/releasing of objects */
	interface AllocatorWithRelease<T> extends Allocator<T> {
		void release(T t);
	}

	/** Interface {@link Factory} supports the creation and initialization of objects */
	interface Factory<T> {
		/** Create new object */
		T create();

		/** Initialize specified object 
		 * (needed for cleanup after use, it will be in an equivalent state like an object returned by {@link #create}
		 */
		T init(T t);
	}

	static class Buffer {
		static final int SIZE = 1024 * 1024;

		byte[] data = new byte[SIZE];
		int len;

		int size() {
			return len;
		}

		void add(byte add) {
			data[len] = add;
			len = (len + 1) % SIZE;
		}
	}

	static class BufferFactory implements Factory<Buffer> {

		@Override
		public Buffer create() {
			return new Buffer();
		}

		@Override
		public Buffer init(Buffer buf) {
			return buf;
		}
	}

	static abstract class BaseAllocator {
		Factory<Buffer> factory = new BufferFactory();
	}

	/** Class {@link NewAllocator} allocates for each request a new object */
	static class NewAllocator extends BaseAllocator implements Allocator<Buffer> {

		@Override
		public Buffer get() {
			return factory.create();
		}
	}

	/**
	 * Class {@link ThreadLocalAllocator} stores allocated objects in a {@link ThreadLocal} buffer.
	 */
	static class ThreadLocalAllocator extends BaseAllocator implements Allocator<Buffer> {

		static final ThreadLocal<Buffer> threadLocal = new ThreadLocal<>();

		@Override
		public Buffer get() {
			Buffer buf = threadLocal.get();
			if (buf == null) {
				buf = factory.create();
				threadLocal.set(buf);
			} else {
				factory.init(buf);
			}
			return buf;
		}
	}

	/**
	 * Class {@link ThreadLocalAllocator} stores allocated objects in a {@link ConcurrentHashMap}.
	 */
	static class ConcurrentMapAllocator extends BaseAllocator implements Allocator<Buffer> {

		static final ConcurrentMap<Long, Buffer> map = new ConcurrentHashMap<>();

		@Override
		public Buffer get() {
			long id = Thread.currentThread().getId();
			return map.compute(id, (k, v) -> (v == null) ? factory.create() : factory.init(v));
		}
	}

	/**
	 * Class {@link PoolAllocator} implements a pool allocator.
	 */
	static class PoolAllocator extends BaseAllocator implements AllocatorWithRelease<Buffer> {

		BlockingQueue<Buffer> pool = new LinkedBlockingQueue<>();

		@Override
		public Buffer get() {
			Buffer buf = pool.poll();
			if (buf == null) {
				buf = factory.create();
			} else {
				factory.init(buf);
			}
			return buf;
		}

		@Override
		public void release(Buffer t) {
			pool.add(t);
		}
	}

}
