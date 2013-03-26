package com.ipeer.iutil.awesome.enderchests;

public class Enderchest {
	
	private int colour1;
	private int colour2;
	private int colour3;
	private String registrar = "Nobody";
	private String usage = "No usage specified.";

	public Enderchest(int c1, int c2, int c3, String registrar, String use) {
		this.colour1 = c1;
		this.colour2 = c2;
		this.colour3 = c3;
		this.registrar = registrar;
		this.usage = use;
	}
	
	public Enderchest(String c1, String c2, String c3, String registrar, String use) {
		this(getIntForColour(c1), getIntForColour(c2), getIntForColour(c3), registrar, use);
	}

	public String getRegistrar() {
		return this.registrar;
	}
	
	public String getUsage() {
		return this.usage;
	}
	
	public int[] getColours() {
		return new int[] {this.colour1, this.colour2, this.colour3};
	}

	public String getC1() {
		return Integer.toString(this.colour1);
	}
	
	public String getC2() {
		return Integer.toString(this.colour2);
	}
	
	public String getC3() {
		return Integer.toString(this.colour3);
	}
	
/*	public int getC1() {
		return Integer.toString(this.colour1);
	}
	
	public int getC2() {
		return Integer.toString(this.colour2);
	}
	
	public int getC3() {
		return Integer.toString(this.colour3);
	}*/
	
	private static int getIntForColour(String colour) {
		String c = colour.toLowerCase();
		//System.err.println("-> "+c);
		if (c.matches("w(h(ite)?)?"))
			return 0;
		else if (c.matches("orange"))
			return 1;
		else if (c.matches("magenta"))
			return 2;
		else if (c.matches("l(ight)?blue?"))
			return 3;
		else if (c.matches("yellow?"))
			return 4;
		else if (c.matches("li(me|ght)?green"))
			return 5;
		else if (c.matches("fabulous|pink"))
			return 6;
		else if (c.matches("d(ark)?gr[ea]y"))
			return 7;
		else if (c.matches("li(ght)?gr[ea]y"))
			return 8;
		else if (c.matches("cyan"))
			return 9;
		else if (c.matches("purple"))
			return 10;
		else if (c.matches("(royal)?blue?"))
			return 11;
		else if (c.matches("(poop|shit|crap)?brown"))
			return 12;
		else if (c.matches("(dark|cactus)?green"))
			return 13;
		else if (c.matches("(blood)?red"))
			return 14;
		else
			return 15;
	}
	
}
