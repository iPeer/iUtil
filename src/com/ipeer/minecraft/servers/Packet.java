package com.ipeer.minecraft.servers;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public class Packet {

	public static String readLine(DataInputStream in, int maxLen) throws IOException {
		StringBuilder stringbuilder = new StringBuilder();
		short word = 0;
		try  {
			word = in.readShort();
		}
		catch (EOFException e) {
			return stringbuilder.toString();
		}


		if (word > maxLen)
			throw new IOException("THE STRING IS TOO LONG!");

		if (word < 0) 
			throw new IOException("What...? The string has a length < 0!");

		for (int i = 0; i < word; i++)
		{
			stringbuilder.append(in.readChar());
		}

		return stringbuilder.toString();
	}
}
