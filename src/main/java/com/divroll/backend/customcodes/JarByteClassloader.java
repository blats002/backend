/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend.customcodes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class JarByteClassloader extends ClassLoader {
	public byte[] bytes;

	public JarByteClassloader(byte[] bytes) throws IOException {
		// TODO Validate if the byte array passed is a Jar
		//super(bytes.getClass().getClassLoader());
		super(Thread.currentThread().getContextClassLoader());
		this.bytes = bytes;
	}

	@Override
    protected Class findClass(String name) throws ClassNotFoundException {
		try {
			JarInputStream jis = new JarInputStream(new ByteArrayInputStream(bytes)); 
			JarEntry entry = jis.getNextJarEntry();
			if (entry == null){ 
				throw new ClassNotFoundException(name);
			}
			while (entry != null){
				if (entry.getName().equals(name.replace('.', '/') + ".class")){
					byte[] array = new byte[1024 * 10];
					InputStream is = jis;
					ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
					int length = is.read(array);
			        while (length > 0) {
			        	out.write(array, 0, length);
			        	length = is.read(array);
			        }
		         	return defineClass(name, out.toByteArray(), 0, out.size());
				}
				entry = jis.getNextJarEntry();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; 
	}	
}
