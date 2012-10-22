package com.ipeer.iutil.engine;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> a = new HashMap<String, String>();
		a.put("Test", "Value");
		a.put("Test2", "Value2");
		a.remove(a.keySet().iterator().next());
		for (String b : a.keySet())
			System.err.println(b);
	}

}
