package com.divroll.webdash.server.util;

import java.util.regex.Pattern;

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
}
