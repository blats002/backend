package com.divroll.bucket.validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrivateKeyValidatorTest {


    private static final String PRIVATE_KEY = "";

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void doTest() {
        boolean valid = PrivateKeyValidator.validate(PRIVATE_KEY);
        assertTrue(valid);
    }
}
