/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Simple new: allocation-free for {@literal >=} Java 8
 */
public class Example_13_ReturnNew {

	static class Point {
		double x;
		double y;

		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	static class Service {
		Point scale(Point pt, double factor) {
			return new Point(pt.x * factor, pt.y * factor);
		}
	}

	static final Service service = new Service();

	@State(Scope.Benchmark)
	public static class MyState {
		int count;

		Point getPoint() {
			return new Point(count++, count++);
		}
	}

	@Benchmark
	public double testScale(MyState state) {
		Point pt = service.scale(state.getPoint(), 1.1);
		return pt.x * pt.y;
	}
}