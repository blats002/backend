package com.divroll.webdash.server.resource.gae;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.divroll.webdash.server.guice.GuiceConfigModule;
import com.divroll.webdash.server.util.RegexHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceTestRunner.class)
@GuiceModules(GuiceConfigModule.class)
public class SubdomainRegexTest {
    @Test
    public void test(){
        String host = "demo.localhost.com";
        String subdomain = RegexHelper.parseSubdomain(host, "localhost.com");
        assertEquals("demo", subdomain);
    }
}
