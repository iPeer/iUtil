package com.ipeer.iutil.engine;

import com.ipeer.iutil.engine.Channel;
import com.ipeer.iutil.engine.Engine;

public class Utils {

	protected Engine engine;

	public Utils(Engine engine2) {
		this.engine = engine2;
	}

	public boolean addressesEqual(Channel c, String n) {
		String mynick = engine.MY_NICK;
		String a1 = c.getUserList().get(n).getAddress();
		String a2 = c.getUserList().get(mynick).getAddress();
		return a1.equals(a2);
	}

	public boolean isAdmin(Channel c, String n) {
		return c.getUserList().get(n).isOp() || addressesEqual(c, n);
	}

	public boolean addressesEqual(Channel c, String n, String n2) {
		String a1 = c.getUserList().get(n).getAddress();
		String a2 = c.getUserList().get(n2).getAddress();
		return a1.equals(a2);
	}

}
