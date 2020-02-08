/**
 * Copyright 2017 Divroll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * █████╗ ██████╗ ██╗██████╗ ██╗      █████╗ ███████╗████████╗
 * ██╔══██╗██╔══██╗██║██╔══██╗██║     ██╔══██╗██╔════╝╚══██╔══╝
 * ███████║██████╔╝██║██████╔╝██║     ███████║███████╗   ██║
 * ██╔══██║██╔═══╝ ██║██╔══██╗██║     ██╔══██║╚════██║   ██║
 * ██║  ██║██║     ██║██████╔╝███████╗██║  ██║███████║   ██║
 * ╚═╝  ╚═╝╚═╝     ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝   ╚═╝
 * ----------------------------------------------------------
 * apiBlast - a ParsePlatform prototype powered with Java
 * ----------------------------------------------------------
 *
 */
package com.divroll.backend.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Helper class for handling a most common subset of ISO 8601 strings
 * (in the following format: "2008-03-01T13:00:00+01:00"). It supports
 * parsing the "Z" timezone, but many other less-used features are
 * missing.
 */
public final class ISO8601 {
    /** Transform Calendar to ISO 8601 string. */
    public static String fromCalendar(final Calendar calendar) {
        Date date = calendar.getTime();
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /** Get current date and time formatted as ISO 8601 string. */
    public static String now() {
        return fromCalendar(GregorianCalendar.getInstance());
    }

    /** Transform ISO 8601 string to Calendar. */
    public static Calendar toCalendar(final String iso8601string, String timeZone)
            throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        /*
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ").parse(s);
        */
//        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//        Date date = df1.parse(iso8601string);
        Date date = Date.from( Instant.parse( iso8601string ));
        calendar.setTime(date);
        return calendar;
    }

    public static Date toDate(final String iso8601string, String timeZone) {
        String s = iso8601string; //iso8601string.replace("Z", timeZone);
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        try {
            Date date = isoFormat.parse(s);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String format(final Date date, String timeZone) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        try {
            String formatted = isoFormat.format(date);
            return formatted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String calculateOffset(final String iso8601string, int offset, String timeZone) {
        String s = iso8601string;
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //isoFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        try {
            Date date = isoFormat.parse(s);
            date.setTime(date.getTime() - (3600 * (offset * 1000))); // minus offset hours
            return isoFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
