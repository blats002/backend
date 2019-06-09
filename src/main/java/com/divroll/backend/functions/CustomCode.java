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
package com.divroll.backend.functions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import com.divroll.backend.customcode.method.CustomCodeMethod;
import com.divroll.backend.customcode.jar.JarEntryObject;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import org.json.simple.JSONValue;

/**
 * Class that executes a {@code CustomCodeMethod}
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class CustomCode {
	
	private static Logger LOGGER = Logger.getLogger(CustomCode.class.getName());
	private static String MAIN_CLASS = "Main-Class";
	//private CustomCodeEventListener listener;
	private CompletableFuture<Map<String,?>> future;
	private byte[] jar;
	    
	public CustomCode() {
	}
	
//	public CustomCode(byte[] jar, CustomCodeEventListener listener) {
//		this.listener = listener;
//		this.jar = jar;
//	}

	public CustomCode(byte[] jar, CompletableFuture<Map<String,?>> future) {
		this.jar = jar;
		this.future = future;
	}
	
	/**
	 * Execute the main class defined in the {@code pom.xml} 
	 * 
	 * @param request
	 */
	public void executeMainClass(CustomCodeRequest request) {
		LOGGER.info("Execute main class");
		String classToLoad = null;
		String methodName = request.getMethodName();
		try {
			classToLoad = extractMainClassManifest(jar);
			LOGGER.info("Class to load: " + classToLoad);
			JarByteClassloader loader = new JarByteClassloader(jar);
			Class c = loader.loadClass(classToLoad);
			Thread.currentThread().setContextClassLoader(loader);
			JarEntryObject jarEntry = (JarEntryObject) c.newInstance();
        	List<CustomCodeMethod> methods = jarEntry.methods();
        	for (CustomCodeMethod cc : methods){
        		String ccMethodName = cc.getMethodName();
        		if(methodName.equals(ccMethodName)) {
					Map<String, ?> result = cc.execute(request).getResponseMap();
					if (result != null){
						//listener.onSuccess(result); // TODO: remove this
						future.complete(result);
					}
					LOGGER.info("Result: " + JSONValue.toJSONString(result));
				}
        	}

		} catch (ClassNotFoundException e) {
			//listener.onFailure(new EntryPointClassNotFound(classToLoad));
			future.completeExceptionally(e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			//listener.onFailure(new CustomCodeException(e.getMessage()));
			future.completeExceptionally(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			//listener.onFailure(new CustomCodeException(e.getMessage()));
			future.completeExceptionally(e);
			e.printStackTrace();
		} catch (Exception e){
			//listener.onFailure(new CustomCodeException(e.getMessage()));
			future.completeExceptionally(e);
			e.printStackTrace();
		}
	}
	/**
	 * Extracts the POM XML string from a jar bytes
	 * 
	 * @param bytes
	 * @return the String represented pom.xml
	 */
	private String extractMainClassManifest(byte[] bytes){   
		String mainClass = null;
		try {
			JarInputStream jis = new JarInputStream(new ByteArrayInputStream(bytes)); 
			final Manifest manifest = jis.getManifest();
            final Attributes mattr = manifest.getMainAttributes();
            for (Object a : mattr.keySet()) {
            	String attr = String.valueOf(a);
            	if (attr.equals(MAIN_CLASS)){
            		mainClass = mattr.getValue((Name) a);
            		LOGGER.info(attr + " : " + mainClass);
            	}
            }
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return mainClass;
	}
}
