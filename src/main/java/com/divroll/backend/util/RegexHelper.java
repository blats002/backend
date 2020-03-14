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

package com.divroll.backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class RegexHelper {

    public static boolean isNumeric(String string){
        Pattern pattern = Pattern.compile("^[0-9]+$");
        return pattern.matcher(string).matches();
    }

    public static boolean isNumeric(String... strings){
        Pattern pattern = Pattern.compile("^[0-9]+$");
        for(String s : strings){
            if(s != null){
                boolean matches = pattern.matcher(s).matches();
                if(!matches){
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }

    public static String removeQueryParam(String string) {
        Pattern pattern = Pattern.compile("/([^\\?]+)(\\?.*)?/g");
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()){
            return matcher.group(1);
        }
        return string;
    }

    public static String parseSubdomain(String string, String domain){
        Pattern pattern = Pattern.compile("(.*)." + domain);
        if(pattern.matcher(string).matches()){
            return string.substring(0, string.indexOf("." + domain));
        }
        return null;
    }

    public static String parseSubdomainFrompath(String path){
        Pattern pattern = Pattern.compile("/([a-zA-Z0-9]*)");
        Matcher matcher = pattern.matcher(path);
        if(matcher.find()){
            return matcher.group(1);  // Here, use Group 1 value
        }
        return null;
    }

    public static String parseFileName(String path){
        Pattern pattern = Pattern.compile(".*/(.*)");
        Matcher matcher = pattern.matcher(path);
        if(matcher.matches()){
            return matcher.group(1);
        }
        return null;
    }

    public static boolean isDirectory(String path){
        Pattern pattern = Pattern.compile(".*\\/([\\w-]+\\.)");
        Matcher matcher = pattern.matcher(path);
        if(!matcher.matches()){
            return true;
        }
        return false;
    }

    public static boolean isPath(String path) {
        Pattern pattern = Pattern.compile(".+\\/[a-z0-9-_]+$");
        Matcher matcher = pattern.matcher(path);
        if(matcher.matches()){
            return true;
        }
        return false;
    }

    public static String getRef(String path) {
        Pattern pattern = Pattern.compile("\\/([a-z0-9-_]+)$");
        Matcher matcher = pattern.matcher(path);
        if(matcher.matches()){
            return matcher.group(1);
        }
        return "";
    }

}
