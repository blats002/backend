package com.divroll.core.rest;

import com.divroll.core.rest.util.StringUtil;
import net.spy.memcached.AddrUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Created by Kerby on 7/20/2017.
 */
public class TestUtils {
    @Test
    public void testAddrUtil() {
        String input = "127.0.0.1:11211,127.0.0.1:11211,127.0.0.1:11211,127.0.0.1:11211";
        List<String> address = StringUtil.asList(input);
        List list = AddrUtil.getAddresses(address);

        assertEquals(4, address.size());
        assertEquals(4, list.size());
    }
    @Test
    public void testAddrUtilSingle() {
        String input = "localhost:11211";
        List<String> address = StringUtil.asList(input);
        List list = AddrUtil.getAddresses(address);

        assertNotNull(list);
        assertEquals(1, address.size());
        assertEquals(1, list.size());
    }
}
