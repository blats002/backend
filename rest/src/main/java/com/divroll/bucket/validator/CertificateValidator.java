package com.divroll.bucket.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CertificateValidator {
    private static Pattern pattern;
    private static Matcher matcher;
    private static final String regex = "(?m)^-{3,}BEGIN CERTIFICATE-{3,}$(?s).*?^-{3,}END CERTIFICATE-{3,}$";
    public static boolean validate(final String certificate) {
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(certificate);
        return matcher.matches();
    }
}
