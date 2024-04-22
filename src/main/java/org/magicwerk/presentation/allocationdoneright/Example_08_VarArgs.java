/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright;

import java.util.Map;

import org.magicwerk.brownies.core.CollectionTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Variable arguments: <br>
 * - allocation-free for {@literal >=} Java 17
 */
public class Example_08_VarArgs {

	static class StrlenService {
		int getLength(String... strs) {
			int len = 0;
			for (int i = 0; i < strs.length; i++) {
				len += strs[i].length();
			}
			return len;
		}
	}

	static final StrlenService service = new StrlenService();

	@State(Scope.Benchmark)
	public static class MyState {
		String s1 = "a";
		String s2 = "b";

		Map<Integer, String> map = CollectionTools.createHashMap(1, "a", 2, "b");
	}

	@Benchmark
	public int test(MyState state) {
		return service.getLength(state.s1, state.s2);
	}

	// Show that varargs are also allocation free if not constructed from constants but from method calls
	@Benchmark
	public int test2(MyState state) {
		return service.getLength(state.map.get(1), state.map.get(2));
	}
}