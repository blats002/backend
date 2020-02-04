/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.core.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CachingOutputStream extends OutputStream {
	private final OutputStream os;
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public CachingOutputStream(OutputStream os) {
		this.os = os;
	}

	public void write(int b) throws IOException {
		try {
			os.write(b);
			baos.write(b);
		} catch (Exception e) {

		}
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

