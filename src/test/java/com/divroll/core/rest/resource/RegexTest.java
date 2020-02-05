/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
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
package com.divroll.core.rest.resource;

import com.carlosbecker.guice.GuiceTestRunner;
//import com.divroll.core.rest.guice.GuiceConfigModule;
import com.divroll.backend.util.RegexHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceTestRunner.class)
//@GuiceModules(GuiceConfigModule.class)
public class RegexTest {
    @Test
    public void testSubdomain(){
        String host = "demo.localhost.com";
        String subdomain = RegexHelper.parseSubdomain(host, "localhost.com");
        assertEquals("demo", subdomain);
    }
    @Test
    public void testSubdomainFromPath(){
        String path = "/www/index.html";
        String subdomain = RegexHelper.parseSubdomainFrompath(path);
        assertEquals("www", subdomain);
    }
    @Test
    public void testFilename(){
        String path = "/demo/test.txt";
        String fileName = RegexHelper.parseFileName(path);
        assertEquals("test.txt", fileName);

        path = "/demo1/demo2/demo.txt";
        fileName = RegexHelper.parseFileName(path);
        assertEquals("demo.txt", fileName);

        path = "/demo1/demo2/demo";
        fileName = RegexHelper.parseFileName(path);
        assertEquals("demo", fileName);
    }

    @Test
    public void testIsFolder(){
        String path = "/demo1/demo2/demo";
        boolean isDirectory = RegexHelper.isDirectory(path);
        assertTrue(isDirectory);

        path = "/demo1/demo2/demo.3";
        isDirectory = RegexHelper.isDirectory(path);
        assertTrue(isDirectory);
    }
}
