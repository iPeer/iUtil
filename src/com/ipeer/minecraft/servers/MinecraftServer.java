package com.ipeer.minecraft.servers;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class MinecraftServer {
	
	private String address;
	private int port;

	public MinecraftServer(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}

	public SocketAddress InetSocketAddress() {
		return new InetSocketAddress(this.address, this.port);
	}
	
}
