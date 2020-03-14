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
package com.divroll.backend;

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
