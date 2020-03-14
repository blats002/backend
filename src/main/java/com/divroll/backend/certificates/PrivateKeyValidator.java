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
