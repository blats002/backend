/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.util;

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

}
