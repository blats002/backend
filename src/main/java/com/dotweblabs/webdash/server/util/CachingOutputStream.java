package com.divroll.webdash.server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CachingOutputStream extends OutputStream
{
	private final OutputStream os;
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public CachingOutputStream(OutputStream os) {
		this.os = os;
	}

	public void write(int b) throws IOException {
		os.write(b);
		baos.write(b);
	}

	public byte[] getCache() {
		return baos.toByteArray();
	}

	public void close() throws IOException {
		os.close();
	}

	public void flush() throws IOException {
		os.flush();
	}

}

