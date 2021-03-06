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
package com.divroll.backend.customcodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import com.divroll.backend.customcode.method.CustomCodeMethod;
import com.divroll.backend.customcode.jar.JarEntryObject;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.divroll.backend.customcode.rest.CustomCodeResponse;
import com.divroll.backend.sdk.exception.BadRequestException;
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
	private CompletableFuture<CustomCodeResponse> future;
	private byte[] jar;
	    
	public CustomCode() {
	}
	
//	public CustomCode(byte[] jar, CustomCodeEventListener listener) {
//		this.listener = listener;
//		this.jar = jar;
//	}

	public CustomCode(byte[] jar, CompletableFuture<CustomCodeResponse> future) {
		this.jar = jar;
		this.future = future;
	}
	
	/**
	 * Execute the main class defined in the {@code pom.xml} 
	 * 
	 * @param request
	 */
	public void executeMainClass(CustomCodeRequest request, int timeoutInSeconds) {
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
        	boolean isMethodExists = false;
        	for (CustomCodeMethod cc : methods){
        		String ccMethodName = cc.getMethodName();
        		if(methodName.equals(ccMethodName)) {
        			isMethodExists = true;
					ExecutorService executor = Executors.newCachedThreadPool();
					Callable<CustomCodeResponse> task = new Callable<CustomCodeResponse>() {
						@Override
						public CustomCodeResponse call() throws Exception {
							CustomCodeResponse customCodeResponse = cc.execute(request);
							//Map<String, ?> result = cc.execute(request).getResponseMap();
							return customCodeResponse;
						}
					};
					Future<CustomCodeResponse> futureResult = executor.submit(task);
					//CustomCodeResponse result = futureResult.get(timeoutInSeconds, TimeUnit.SECONDS);
					CustomCodeResponse result = futureResult.get();
					if (result != null){
						//listener.onSuccess(result); // TODO: remove this
						future.complete(result);
					}
					//LOGGER.info("Result: " + JSONValue.toJSONString(result));
				}
        	}

        	if(!isMethodExists) {
				future.cancel(true);
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
			future.completeExceptionally(e);
			e.printStackTrace();
		} /*catch (TimeoutException e){
			future.completeExceptionally(e);
			e.printStackTrace();
		}*/ catch (Exception e){
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
