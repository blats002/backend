/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */

package com.divroll.backend.certificates;

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
