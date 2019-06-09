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
package com.divroll.backend.functions.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class Utils {
	private static Logger LOGGER = Logger.getLogger(Utils.class.getName());
	/**
	 * Create a temporary file for the jar bytes
	 * 
	 * @param bytes
	 * @return
	 */
	public static File createFileFromBytes(byte[] bytes) {
		String tempFileName = "myjar.jar"; // FIXME make file name generated as GUID
        try {
		    FileOutputStream fileOuputStream = new FileOutputStream(tempFileName); 
		    fileOuputStream.write(bytes);
		    fileOuputStream.flush();
		    fileOuputStream.close();
        }catch(Exception e){
            LOGGER.info(e.getMessage());
        }
        return new File(tempFileName);
	}
    public static byte[] fileToBytes(File file){
    	byte[] b = new byte[(int) file.length()];
    	try {
    		FileInputStream fileInputStream = new FileInputStream(file);
    		fileInputStream.read(b);
    	} catch (FileNotFoundException e) {
    		LOGGER.info(e.getMessage());
    	}
    	catch (IOException e1)
    	{
    		LOGGER.info("Error reading the file.");
    	}
    	return b;
    }	
}
