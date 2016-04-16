/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.util;

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

