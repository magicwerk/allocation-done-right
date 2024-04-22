package org.magicwerk.presentation.allocationdoneright.misc;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class CheckInlineWrapper {

	interface BaseI {
		int length();

		String get();
	}

	public static class BaseC<E> {
		E obj;

		BaseC(E obj) {
			this.obj = obj;
		}
	}

	public static class Wrapper extends CheckInlineWrapper.BaseC<String> implements CheckInlineWrapper.BaseI {

		int len;

		Wrapper(String str) {
			super(str);

			// Ok: extract data from argument
			len = str.length();
		}

		@Override
		public String get() {
			return obj;
		}

		@Override
		public int length() {
			return len;
		}
	}

	public static class Wrapper2 extends CheckInlineWrapper.BaseC<String> implements CheckInlineWrapper.BaseI {

		String str2;

		Wrapper2(String str) {
			super(str);

			// Not ok: create new object from argument
			str2 = "(" + str + ")";
		}

		@Override
		public String get() {
			return obj;
		}

		@Override
		public int length() {
			return get().length();
		}
	}

	@State(Scope.Benchmark)
	public static class CheckState {
		final int size = 100;
		IList<String> vals = GapList.create();
		int index;

		public CheckState() {
			for (int i = 0; i < size; i++) {
				vals.add("elem-" + i);
			}
		}

		public String get() {
			String val = vals.get(index);
			index++;
			if (index == size) {
				index = 0;
			}
			return val;
		}

	}

	@Benchmark
	public int handleWrapper(CheckInlineWrapper.CheckState state) {
		return handleWrapper(new Wrapper(state.get()));
	}

	@Benchmark
	public int handleWrapper2(CheckInlineWrapper.CheckState state) {
		return handleWrapper2(new Wrapper2(state.get()));
	}

	@Benchmark
	public int handleWrapperCached(CheckInlineWrapper.CheckState state) {
		return handleWrapperCached(new Wrapper(state.get()));
	}

	@Benchmark
	public int handleWrapperRecursive(CheckInlineWrapper.CheckState state) {
		return handleWrapperRecursive(new Wrapper(state.get()));
	}

	@Benchmark
	public int handleWrapperRecursive2(CheckInlineWrapper.CheckState state) {
		return handleWrapperRecursive2(new Wrapper(state.get()));
	}

	@Benchmark
	public int handleWrapperRecursive3(CheckInlineWrapper.CheckState state) {
		return handleWrapperRecursive3(new Wrapper(state.get()));
	}

	int handleWrapper(CheckInlineWrapper.Wrapper wrapper) {
		return wrapper.get().length();
	}

	int handleWrapper2(CheckInlineWrapper.Wrapper2 wrapper) {
		return wrapper.get().length();
	}

	int handleWrapperRecursive(CheckInlineWrapper.Wrapper wrapper) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum += doHandleWrapperRecursive(wrapper, true);
		}
		return sum;
	}

	int doHandleWrapperRecursive(CheckInlineWrapper.Wrapper wrapper, boolean r) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			if (r) {
				sum += doHandleWrapperRecursive(wrapper, false);
			}
		}
		return sum;
	}

	int handleWrapperRecursive2(CheckInlineWrapper.Wrapper wrapper) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum += doHandleWrapperRecursive2(wrapper, 0);
		}
		return sum;
	}

	int doHandleWrapperRecursive2(CheckInlineWrapper.Wrapper wrapper, int l) {
		int sum = wrapper.length();
		for (int i = 0; i < 10; i++) {
			if (l % 2 == 0) {
				sum += doHandleWrapperRecursive2(wrapper, l + 1);
			}
		}
		return sum;
	}

	int handleWrapperRecursive3(CheckInlineWrapper.Wrapper wrapper) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum += doHandleWrapperRecursive3a(wrapper, 0);
		}
		return sum;
	}

	int doHandleWrapperRecursive3a(CheckInlineWrapper.Wrapper wrapper, int l) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			if (l % 2 == 0 && l < 4) {
				sum += doHandleWrapperRecursive3b(wrapper, l + 1);
			}
		}
		return sum;
	}

	int doHandleWrapperRecursive3b(CheckInlineWrapper.Wrapper wrapper, int l) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			if (l % 2 == 1 && l < 4) {
				sum += doHandleWrapperRecursive3a(wrapper, l + 1);
			}
		}
		return sum;
	}

	static CheckInlineWrapper.Wrapper wrapperCache;

	int handleWrapperCached(CheckInlineWrapper.Wrapper wrapper) {
		wrapperCache = wrapper;
		return wrapper.get().length();
	}

}