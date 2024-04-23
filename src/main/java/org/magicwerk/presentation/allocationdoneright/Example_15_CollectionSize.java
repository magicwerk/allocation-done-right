package org.magicwerk.presentation.allocationdoneright;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;

import ch.qos.logback.classic.Logger;

/**
 * Show how primitive collections save memory by factor 7.
 */
public class Example_15_CollectionSize {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	void run() {
		IList<Integer> integerList = GapList.create();
		IIntList intList = IntGapList.create();
		IList<Integer> intObjList = IntObjGapList.create();

		for (int i = 0; i < 1_000_000; i++) {
			integerList.add(i);
			intList.add(i);
			intObjList.add(i);
		}

		LOG.info("integerList: {} bytes", ReflectTools.getObjectSize(integerList));
		LOG.info("intList:     {} bytes", ReflectTools.getObjectSize(intList));
		LOG.info("intObjList:  {} bytes", ReflectTools.getObjectSize(intObjList));

		// Clear content, but keep container arraz
		integerList.clear();
		LOG.info("integerList: {} bytes (after clear)", ReflectTools.getObjectSize(integerList));

		// Trim container array to size 0 (after clear)
		integerList.trimToSize();
		LOG.info("integerList: {} bytes (after trimToSize)", ReflectTools.getObjectSize(integerList));

		// Allow GC to remove the object altogether
		integerList = null;
	}
}