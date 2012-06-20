package com.ipeer.iutil.engine;

public class MCUtils {

	public static int getExp(int a, String version) {
		if (version.equals("12w23b")) {
			a -= 1;
			int exp = 0;
			for (int x = 0; x <= a; x++) {
				exp += expFormula(x);
			}
			return exp;
		}
		else {
			return (int)(7.0 * a + ((a - 1.0) / 2.0) * a * 3.5 - Math.floor((a / 2.0)) * 0.5);
		}
	}

	public static int expFormula(int level) {
		if (level >= 30) {
			return 62 + (level - 30) * 7;
		}
		if (level >= 15) {
			return 17 + (level - 15) * 3;
		}
		return 17;
	}

}
