package com.ipeer.iutil.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class SocketTest {

	public static void main(String[] args) {
		Socket a = new Socket();
		try {
			a.setSoTimeout(3000);
			a.connect(new InetSocketAddress("login.minecraft.net", 443), 3000);
			System.err.println("Connected.");
			a.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
