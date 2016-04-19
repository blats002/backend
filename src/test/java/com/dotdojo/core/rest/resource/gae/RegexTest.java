package com.divroll.core.rest.resource.gae;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.divroll.core.rest.guice.GuiceConfigModule;
import com.divroll.core.rest.util.RegexHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceTestRunner.class)
@GuiceModules(GuiceConfigModule.class)
public class RegexTest {
    @Test
    public void testSubdomain(){
        String host = "demo.localhost.com";
        String subdomain = RegexHelper.parseSubdomain(host, "localhost.com");
        assertEquals("demo", subdomain);
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
}
