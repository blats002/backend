/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.customcodes.util;

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
