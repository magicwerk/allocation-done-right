package org.magicwerk.presentation.allocationdoneright.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Logger;

/**
 * Show that GC of instances of LinkedList objects takes longer than ArrayList as more pointer must be followed.
 */
public class EvalGcDuration {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	void run() {
		int bytes = 100_000_000;
		ArrayList<Integer> arrayList = allocArrayList(bytes);
		LinkedList<Integer> linkedList = allocLinkedList(bytes);

		LOG.info("ArrayList: {} list elements, size {}, reachable {}", arrayList.size(), ReflectTools.getObjectSize(arrayList),
				ReflectTools.getObjectReachables(arrayList).size());
		LOG.info("LinkedList: {} list elements, size {}, reachable {}", linkedList.size(), ReflectTools.getObjectSize(linkedList),
				ReflectTools.getObjectReachables(linkedList).size());
	}

	static int bytes = 100_000_000;

	@State(Scope.Benchmark)
	public static class CheckStateArrayList {
		ArrayList<Integer> arrayList = allocArrayList(bytes);
	}

	@State(Scope.Benchmark)
	public static class CheckStateLinkedList {
		LinkedList<Integer> linkedList = allocLinkedList(bytes);
	}

	@State(Scope.Benchmark)
	public static class CheckStateByteArray {
		IList<byte[]> byteArrays = allocByteArray(bytes);
	}

	@Fork(jvmArgs = "-Xmx512m")
	@Benchmark
	public int testArrayList(EvalGcDuration.CheckStateArrayList state) {
		int n = 0;
		for (int i = 0; i < 100; i++) {
			byte[] o = new byte[bytes];
			n += o.length;
		}
		return n;
	}

	@Fork(jvmArgs = "-Xmx512m")
	@Benchmark
	public int testLinkedList(EvalGcDuration.CheckStateLinkedList state) {
		int n = 0;
		for (int i = 0; i < 100; i++) {
			byte[] o = new byte[bytes];
			n += o.length;
		}
		return n;
	}

	@Fork(jvmArgs = "-Xmx512m")
	@Benchmark
	public int testByteArray(EvalGcDuration.CheckStateByteArray state) {
		int n = 0;
		for (int i = 0; i < 100; i++) {
			byte[] o = new byte[bytes];
			n += o.length;
		}
		return n;
	}

	static IList<byte[]> allocByteArray(int bytes) {
		IList<byte[]> byteArrays = GapList.create();
		for (int i = 0; i < bytes / 1000; i++) {
			byte[] byteArray = new byte[1000];
			byteArrays.add(byteArray);
		}
		return byteArrays;
	}

	/** Add elements to ArrayList until it has reached the specified size in bytes */
	static ArrayList<Integer> allocArrayList(int bytes) {
		ArrayList<Integer> list = new ArrayList<>();
		allocList(list, bytes);
		return list;
	}

	/** Add elements to LinkedList until it has reached the specified size in bytes */
	static LinkedList<Integer> allocLinkedList(int bytes) {
		LinkedList<Integer> list = new LinkedList<>();
		allocList(list, bytes);
		return list;
	}

	static void allocList(List<Integer> list, int bytes) {
		int n = 0;
		while (true) {
			for (int i = 0; i < 100_000; i++) {
				list.add(n);
				n++;
			}
			int size = ReflectTools.getObjectSize(list);
			LOG.info("{}", size);
			if (size > bytes) {
				break;
			}
		}
	}
}