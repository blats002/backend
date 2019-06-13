package com.divroll.bucket.validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CertificateValidatorTest {


    private static final String CERTIFICATE = "";

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void doTest() {
        boolean valid = CertificateValidator.validate(CERTIFICATE);
        assertTrue(valid);
    }
}
