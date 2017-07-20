package com.divroll.core.rest.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StringUtil {
    /**
     * Generate a List from a comma separated value.
     *
     * @param csv
     * @return
     */
    public static List<String> asList(String csv) {
        List<String> result = new LinkedList<String>();
        String[] array = csv.split(",");
        result = Arrays.asList(array);
        return result;
    }
}
