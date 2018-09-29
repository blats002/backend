package com.divroll.backend;

import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.model.filter.TransactionFilterParser;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;


@RunWith(JUnit4.class)
public class TransactionFilterParserTest extends TestCase{

    private String TEST_DATA = "[\n" +
            "  {\n" +
            "    \"op\" : \"find\",\n" +
            "    \"propertyName\" : \"fullName\",\n" +
            "    \"value\" : \"John Smith\"\n" +
            "  }  \n" +
            "]";
    @Test
    public void test() {
        System.out.println(TEST_DATA);
        List<TransactionFilter> filterList = TransactionFilterParser.parseFilter(TEST_DATA);
    }
}
