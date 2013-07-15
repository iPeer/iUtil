package com.ipeer.iutil.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JSONReader {

	private Map<String, String> data = new HashMap<String, String>();

	public JSONReader(String file) throws EmptyJSONFileException, MalformedURLException, IOException {
		this(new URL(file));
	}

	public JSONReader(URL file) throws IOException, EmptyJSONFileException {
		InputStream in;
		in = file.openStream();
		Scanner s = new Scanner(in, "UTF-8");
		while (s.hasNextLine())
			parse(s.nextLine());
		s.close();
	}

	public JSONReader(File file) throws FileNotFoundException, EmptyJSONFileException {
		InputStream in;
		in = new FileInputStream(file);
		Scanner s = new Scanner(in, "UTF-8");
		while (s.hasNextLine())
			parse(s.nextLine());
		s.close();
	}

	private void parse(String s) throws EmptyJSONFileException {
		if (s.equals("[]"))
			throw new EmptyJSONFileException();
		String[] data1 = s.split(",[\"\\{]");
		for (String a : data1) {
			String[] ddata = a.replaceAll("\\}\\]|\\{\\[|\\[\\{\"|\\}|\\{", "").split("\":");
			System.err.println(ddata[0]+", "+ddata[1]);
			data.put(trim(ddata[0]), trim(ddata[1]));	
		}
	}

	public String get(String key) {
		String r = data.get(key);
		return (r == null ? "" : r);

	}

	private String trim(String s) {
		if (s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length() - 1);
		else if (s.startsWith("\""))
			return s.substring(1);
		else if (s.endsWith("\""))
			return s.substring(0, s.length() - 1);
		return s;
	}

}
