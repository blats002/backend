package com.apiblast.customcode.example;

import java.util.LinkedList;
import java.util.List;

import com.apiblast.customcode.example.methods.HelloMethod;
import com.apiblast.customcode.example.methods.CreateResetPasswordLinkMethod;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.jar.JarEntryObject;

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
		methods.add(new CreateResetPasswordLinkMethod());
		return methods;
	}
}
