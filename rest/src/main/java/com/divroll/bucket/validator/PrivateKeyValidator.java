package com.divroll.bucket.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivateKeyValidator {
    private static Pattern pattern;
    private static Matcher matcher;
    private static final String PRIVATE_KEY_PATTERN = "^(?:(?!-{3,}(?:BEGIN|END) (?:RSA PRIVATE|PRIVATE) KEY)[\\s\\S])*(-{3,}BEGIN (?:RSA PRIVATE|PRIVATE) KEY(?:(?!-{3,}END (?:RSA PRIVATE|PRIVATE) KEY)[\\s\\S])*?-{3,}END (?:RSA PRIVATE|PRIVATE) KEY-{3,})(?![\\s\\S]*?-{3,}BEGIN (?:RSA PRIVATE|PRIVATE) KEY[\\s\\S]+?-{3,}END (?:RSA PRIVATE|PRIVATE) KEY[\\s\\S]*?$)";
    public static boolean validate(final String privateKey) {
        pattern = Pattern.compile(PRIVATE_KEY_PATTERN);
        matcher = pattern.matcher(privateKey);
        return matcher.matches();

    }
}
