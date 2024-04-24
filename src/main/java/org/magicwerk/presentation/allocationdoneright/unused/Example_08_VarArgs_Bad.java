/**
 * 
 */
package org.magicwerk.presentation.allocationdoneright.unused;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Variable arguments: <br>
 * - NOT allocation-free if stored
 */
public class Example_08_VarArgs_Bad {

	static class Strlen {
		final String[] strs;

		Strlen(String... strs) {
			this.strs = strs;
		}

		int getLength() {
			int len = 0;
			for (int i = 0; i < strs.length; i++) {
				len += strs[i].length();
			}
			return len;
		}
	}

	@State(Scope.Benchmark)
	public static class MyState {
		String s1 = "a";
		String s2 = "b";
	}

	@Benchmark
	public int test(MyState state) {
		return new Strlen(state.s1, state.s2).getLength();
	}
}