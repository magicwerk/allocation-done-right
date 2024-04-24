/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.unused;

import java.util.Arrays;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * getAsList: allocation-free for {@literal >=} Java 21
 * getDynamicPoint: never allocation-free
 * others: always allocation free
 */
public class Example_03_ReturnNew_Bad {

	static class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		int get() {
			return x + y;
		}
	}

	static class DynamicPoint {
		int[] points;

		DynamicPoint(int num) {
			points = new int[num];
			for (int i = 0; i < num; i++) {
				points[i] = i;
			}
		}

		int get() {
			return points.length;
		}
	}

	static class Service {

		Point scale(Point pt, int factor) {
			return new Point(pt.x * factor, pt.y * factor);
		}

		Point getPoint(int x, int y) {
			return new Point(x, y);
		}

		DynamicPoint getDynamicPoint(int size) {
			return new DynamicPoint(size);
		}

		List<Integer> getAsList() {
			return Arrays.asList(1, 2);
		}

		int[] getArray() {
			return new int[] { 1, 2 };
		}

		Integer getInteger(int val) {
			return new Integer(val);
		}
	}

	static final Service service = new Service();

	@State(Scope.Benchmark)
	public static class CheckState {
		Point pt = new Point(1, 2);
		int size;
	}

	@Benchmark
	public int testScalePoint(CheckState state) {
		Point pt = service.scale(state.pt, 2);
		return pt.x * pt.y;
	}

	@Benchmark
	public int testGetPoint(CheckState state) {
		Point pt = service.getPoint(1, 2);
		return pt.get();
	}

	@Benchmark
	public int testDynamicPoint(CheckState state) {
		DynamicPoint dp = service.getDynamicPoint(state.size);
		return dp.get();
	}

	@Benchmark
	public int getAsList(CheckState state) {
		List<Integer> list = service.getAsList();
		return list.size();
	}

	@Benchmark
	public int getArray(CheckState state) {
		int[] array = service.getArray();
		return array.length;
	}

	@Benchmark
	public int getInteger(CheckState state) {
		Integer val = service.getInteger(state.size);
		return val.intValue();
	}
}