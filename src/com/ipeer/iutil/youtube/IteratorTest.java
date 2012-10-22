package com.ipeer.iutil.youtube;

import java.util.HashMap;
import java.util.Iterator;

public class IteratorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashMap<String, Integer> a = new HashMap<String, Integer>();
		a.put("Test", 1);
		a.put("Test2", 2);
		a.put("Test3", 3);
		Iterator b = a.keySet().iterator();
		while (b.hasNext())
			System.err.println(b.next());
	}

}
