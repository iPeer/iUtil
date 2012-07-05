package com.ipeer.iutil.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Debug extends PrintStream {
	
	public static PrintStream err = System.err;
	public static PrintStream out = System.out;

	public Debug(OutputStream out) {
		super(out);

	}

	public Debug(String fileName) throws FileNotFoundException {
		super(fileName);

	}

	public Debug(File file) throws FileNotFoundException {
		super(file);

	}

	public Debug(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);

	}

	public Debug(String fileName, String csn) throws FileNotFoundException,
			UnsupportedEncodingException {
		super(fileName, csn);

	}

	public Debug(File file, String csn) throws FileNotFoundException,
			UnsupportedEncodingException {
		super(file, csn);

	}

	public Debug(OutputStream out, boolean autoFlush, String encoding)
			throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);

	}

}
