package com.divroll.hiphop.customcode.example;

import java.util.LinkedList;
import java.util.List;

import com.divroll.hiphop.customcode.example.methods.HelloMethod;
import com.divroll.hiphop.customcode.example.methods.HiMethod;
import com.divroll.hiphop.sdkapi.customcode.CustomCodeMethod;
import com.divroll.hiphop.sdkapi.jar.JarEntryObject;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class MyJarEntryObject extends JarEntryObject
{
	@Override
	public List<CustomCodeMethod> methods() {
		List<CustomCodeMethod> methods = new LinkedList<CustomCodeMethod>();
		methods.add(new HelloMethod());
		methods.add(new HiMethod());
		return methods;
	}
}
