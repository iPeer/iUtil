package com.ipeer.iutil.engine;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeTests {


	public static void main(String[] args) {
		DateFormat d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK);
		long a = System.currentTimeMillis();
		System.err.println(d.format(a));
		System.err.println(d.format(a + 10800000));
		long b = System.currentTimeMillis();
		long c = 10800000;
		long e = 1080000;
		System.err.println(d.format(a + e));
		System.err.println(b+c);
		System.err.println(b + c);
		System.err.println(b + e);

	}

}
