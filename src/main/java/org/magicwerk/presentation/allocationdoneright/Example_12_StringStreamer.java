package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.StringTools;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Java {@literal >8}: StringLatin1 / StringUTF16, i.e. each call like charAt() becomes more complex, so more complex operations like indexOf() should be used
 * allocation-free for {@literal >=} Java 8
 * equal performance for {@literal >=} Java 11, for Java 8 the stateful approach is 25% slower 
 */
public class Example_12_StringStreamer {

	@State(Scope.Benchmark)
	public static class MyState {
		static final int NUM = 10;

		IList<String> texts = GapList.create();
		IList<String> textsNonLatin1 = GapList.create();
		int count;

		public MyState() {
			for (int i = 0; i < NUM; i++) {
				String text = StringTools.repeat("x", 10 + i) + "-";
				texts.add(text);
				String textNonLatin1 = StringTools.repeat("x", 10 + i) + "-" + StringTools.NOT_A_CHAR;
				textsNonLatin1.add(textNonLatin1);
			}
		}

		String getString() {
			String str = texts.get(count);
			count++;
			if (count == NUM) {
				count = 0;
			}
			return str;
		}

		String getStringNonLatin1() {
			String str = textsNonLatin1.get(count);
			count++;
			if (count == NUM) {
				count = 0;
			}
			return str;
		}
	}

	@Benchmark
	public int indexOf(MyState state) {
		return state.getString().indexOf('-');
	}

	@Benchmark
	public int skipUntil(MyState state) {
		return skipUntil(state.getString(), 0, '-');
	}

	@Benchmark
	public int skipUntilNonLatin1(MyState state) {
		return skipUntil(state.getStringNonLatin1(), 0, '-');
	}

	// This flag is not supported by Java 8
	@Fork(jvmArgs = { "-XX:-CompactStrings", "-XX:+IgnoreUnrecognizedVMOptions" })
	@Benchmark
	public int skipUntilNonCompact(MyState state) {
		return skipUntil(state.getString(), 0, '-');
	}

	static int skipUntil(String str, int index, char c) {
		int len = str.length();
		while (index < len) {
			if (str.charAt(index) == c) {
				return index;
			}
			index++;
		}
		return -1;
	}

	@Benchmark
	public int skipUntilWithReader(MyState state) {
		Reader r = new Reader(state.getString());
		r.skipUntil('-');
		return r.pos;
	}

	static class Reader {
		final String str;
		final int len;
		int pos;

		Reader(String str) {
			this.str = str;
			len = str.length();
		}

		void skipUntil(char c) {
			while (pos < len) {
				if (str.charAt(pos) == c) {
					break;
				}
				pos++;
			}
			return;
		}

	}
}